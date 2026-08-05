package com.cardgame.logic;

import com.cardgame.data.CardData;
import com.cardgame.data.StatusEffect;
import com.cardgame.logic.events.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resolves STS combat actions with status effects and card special effects.
 *
 * Status effects applied:
 *   VULNERABLE  – target takes 50% more damage
 *   WEAK        – attacker deals 25% less damage
 *   STRENGTH    – attacker deals +N bonus damage
 *   DEXTERITY   – defender gains +N bonus block
 *   POISON      – handled in TurnManager (ticks at turn start)
 *
 * Card special effects (via CardData.specialEffect()):
 *   ADD_COPY_TO_DISCARD  – add copy of card to discard (Anger)
 *   DAMAGE_EQUALS_BLOCK  – override damage with current player block (Body Slam)
 *   APPLY_VULNERABLE_2   – apply 2 Vulnerable to monster (Bash)
 *   APPLY_WEAK_2         – apply 2 Weak to monster (Intimidate, etc.)
 *   APPLY_POISON_3       – apply 3 Poison to monster
 */
public final class CombatResolver {

    /**
     * Play a card from the player's hand.
     * Returns events produced.
     */
    public List<GameEvent> playCard(GameState state, CardData card) {
        List<GameEvent> events = new ArrayList<>();

        if (state.playerEnergy < card.energyCost()) {
            return events; // not enough energy — no-op
        }

        // Spend energy
        state.playerEnergy -= card.energyCost();

        // Remove from hand, add to discard
        state.hand.remove(card);
        state.discardPile.add(card);

        int damageDealt = 0;
        int blockGained = 0;

        // Apply relic attack boost from RunManager
        int atkBoost = RunManager.getInstance().getTotalAttackBoost();

        // ── Damage calculation ──────────────────────────────────
        int baseDamage = card.damage();

        // Body Slam special: damage = current player block
        if ("DAMAGE_EQUALS_BLOCK".equals(card.specialEffect())) {
            baseDamage = state.playerBlock;
        }

        if (baseDamage > 0 || "DAMAGE_EQUALS_BLOCK".equals(card.specialEffect())) {
            int rawDamage = baseDamage + atkBoost;

            // Apply STRENGTH bonus
            rawDamage += state.playerStatus.get(StatusEffect.STRENGTH);

            // Apply WEAK: player deals 25% less damage
            if (state.playerStatus.has(StatusEffect.WEAK)) {
                rawDamage = (int)(rawDamage * 0.75f);
            }

            // Apply VULNERABLE: monster takes 50% more damage
            if (state.monsterStatus.has(StatusEffect.VULNERABLE)) {
                rawDamage = (int)(rawDamage * 1.5f);
            }

            // Damage absorbed by monster block first
            if (state.monsterBlock > 0) {
                if (rawDamage <= state.monsterBlock) {
                    state.monsterBlock -= rawDamage;
                    rawDamage = 0;
                } else {
                    rawDamage -= state.monsterBlock;
                    state.monsterBlock = 0;
                    state.monsterHp -= rawDamage;
                    damageDealt = rawDamage;
                }
            } else {
                state.monsterHp -= rawDamage;
                damageDealt = rawDamage;
            }
            if (damageDealt > 0) {
                events.add(new DamageDealtEvent("player", "monster", damageDealt));
            }
        }

        // ── Block calculation ───────────────────────────────────
        if (card.defence() > 0) {
            int defBoost = RunManager.getInstance().getTotalDefenceBoost();
            blockGained = card.defence() + defBoost;

            // Apply DEXTERITY: +N to block gained
            blockGained += state.playerStatus.get(StatusEffect.DEXTERITY);

            state.playerBlock += blockGained;
            events.add(new BlockGainedEvent("player", blockGained));
        }

        events.add(0, new CardPlayedEvent(card, damageDealt, blockGained));

        // ── Special effects ─────────────────────────────────────
        String fx = card.specialEffect() == null ? "" : card.specialEffect();
        switch (fx) {
            case "ADD_COPY_TO_DISCARD":
                state.discardPile.add(card);
                break;
            case "APPLY_VULNERABLE_2":
                state.monsterStatus.apply(StatusEffect.VULNERABLE, 2);
                events.add(new StatusEffectAppliedEvent("monster", StatusEffect.VULNERABLE, 2));
                break;
            case "APPLY_WEAK_2":
                state.monsterStatus.apply(StatusEffect.WEAK, 2);
                events.add(new StatusEffectAppliedEvent("monster", StatusEffect.WEAK, 2));
                break;
            case "APPLY_POISON_3":
                state.monsterStatus.apply(StatusEffect.POISON, 3);
                events.add(new StatusEffectAppliedEvent("monster", StatusEffect.POISON, 3));
                break;
            default:
                break;
        }

        // Check win
        state.checkWinCondition().ifPresent(events::add);

        return events;
    }

    /**
     * Execute the monster's turn based on its rolled intent.
     */
    public List<GameEvent> executeMonsterTurn(GameState state) {
        List<GameEvent> events = new ArrayList<>();

        if ("ATTACK".equals(state.intentType)) {
            int damage = state.intentValue;

            // Monster WEAK: deals 25% less
            if (state.monsterStatus.has(StatusEffect.WEAK)) {
                damage = (int)(damage * 0.75f);
            }
            // Player VULNERABLE: takes 50% more
            if (state.playerStatus.has(StatusEffect.VULNERABLE)) {
                damage = (int)(damage * 1.5f);
            }

            // Damage absorbed by player block first
            if (state.playerBlock > 0) {
                if (damage <= state.playerBlock) {
                    state.playerBlock -= damage;
                    damage = 0;
                } else {
                    damage -= state.playerBlock;
                    state.playerBlock = 0;
                }
            }
            if (damage > 0) {
                state.playerHp -= damage;
                events.add(new DamageDealtEvent("monster", "player", damage));
                events.add(new PlayerDamagedEvent(damage));
            }
        } else if ("DEFEND".equals(state.intentType)) {
            state.monsterBlock += state.intentValue;
            events.add(new BlockGainedEvent("monster", state.intentValue));
        }

        // Check loss
        state.checkWinCondition().ifPresent(events::add);

        // Roll next intent
        state.rollMonsterIntent();
        events.add(new MonsterIntentEvent(state.intentType, state.intentValue));

        return events;
    }
}
