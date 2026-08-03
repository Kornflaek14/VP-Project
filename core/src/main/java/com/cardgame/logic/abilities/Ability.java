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
     * Called after the card is placed on the board (Battlecry equivalent).
     */
    default List<GameEvent> onPlay(CardInstance source, GameState state) {
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
}
