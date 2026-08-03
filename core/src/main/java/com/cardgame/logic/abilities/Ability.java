package com.cardgame.logic.abilities;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.GameEvent;

import java.util.List;

/**
 * Strategy interface for card abilities triggered by game events.
 * <p>
 * All hook methods have default no-op implementations so implementors only
 * override the hooks they care about.
 * <p>
 * HARD RULE: no libGDX imports in this package or its sub-packages.
 */
public interface Ability {

    /**
     * Called after the card is played (Battlecry/Spell effect). target can be null.
     */
    default List<GameEvent> onPlayTargeted(CardInstance source, CardInstance target, GameState state) {
        return List.of();
    }

    /**
     * Called each time the source minion performs an attack.
     */
    default List<GameEvent> onAttack(CardInstance source, CardInstance target, GameState state) {
        return List.of();
    }

    /**
     * Called after the source minion's health drops to or below 0 (Deathrattle equivalent).
     * The card has already been removed from the board when this fires.
     */
    default List<GameEvent> onDeath(CardInstance source, GameState state) {
        return List.of();
    }

    /**
     * Called at the start of the owning player's turn while the card is on the board.
     */
    default List<GameEvent> onTurnStart(CardInstance source, GameState state) {
        return List.of();
    }

    /**
     * Allows a sigil to override which slots this card attacks.
     * By default, returns null (meaning the card attacks the slot directly in front of it).
     */
    default List<Integer> getAttackTargets(CardInstance attacker, GameState state, int attackerSlot) {
        return null;
    }

    /**
     * Allows a sigil to bypass an opposing defender and attack the scale directly.
     * (e.g. Airborne bypassing non-Mighty Leap defenders).
     */
    default boolean canAttackDirectly(CardInstance attacker, CardInstance defender) {
        return false;
    }
}
