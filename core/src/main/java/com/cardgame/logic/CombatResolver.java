package com.cardgame.logic;

import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.logic.events.*;

import java.util.ArrayList;
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
 * Card special effects (via AbstractCard.specialEffect()):
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
    public List<GameEvent> playCard(GameState state, AbstractCard card, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        if (state.playerEnergy < card.energyCost()) return events;
        state.playerEnergy -= card.energyCost();
        state.hand.remove(card);
        state.discardPile.add(card);
        events.add(new CardPlayedEvent(card, 0, 0));
        RunManager.getInstance().getRelics().forEach(r -> r.onPlayCard(card, state));
        events.addAll(card.use(state, target));
        state.checkWinCondition().ifPresent(events::add);
        return events;
    }

    /**
     * Execute the monster's turn based on its rolled intent.
     */
        public List<GameEvent> executeMonsterTurn(GameState state) {
        List<GameEvent> events = new ArrayList<>();
        if (state.monsterGroup != null) {
            for (com.cardgame.logic.monsters.AbstractMonster m : state.monsterGroup.monsters) {
                if (m.currentHp > 0) {
                    m.takeTurn(state);
                }
            }
        }
        state.checkWinCondition().ifPresent(events::add);
        return events;
    }
}
