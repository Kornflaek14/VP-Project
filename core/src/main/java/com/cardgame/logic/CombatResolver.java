package com.cardgame.logic;

import com.cardgame.data.CardData;
import com.cardgame.logic.events.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resolves STS combat actions.
 * - Play attack card → deal damage to monster (reduced by monster block)
 * - Play skill card → gain block
 * - Monster turn → monster attacks player or gains block
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

        // Apply damage to monster
        if (card.damage() > 0) {
            int rawDamage = card.damage() + atkBoost;
            // Damage absorbed by monster block first
            if (state.monsterBlock > 0) {
                if (rawDamage <= state.monsterBlock) {
                    state.monsterBlock -= rawDamage;
                    damageDealt = 0;
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

        // Apply block to player
        if (card.defence() > 0) {
            int defBoost = RunManager.getInstance().getTotalDefenceBoost();
            blockGained = card.defence() + defBoost;
            state.playerBlock += blockGained;
            events.add(new BlockGainedEvent("player", blockGained));
        }

        events.add(0, new CardPlayedEvent(card, damageDealt, blockGained));

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
