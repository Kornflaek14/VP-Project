package com.cardgame.logic.actions;

import com.cardgame.logic.GameState;
import com.cardgame.logic.events.GameEvent;

import java.util.List;

/**
 * Command-pattern interface for every action a player can take.
 * <p>
 * Implementors encapsulate all parameters needed to execute the action at
 * construction time.  {@link #execute} validates preconditions, mutates
 * {@link GameState}, and returns the resulting events so callers can log or
 * queue them.
 * <p>
 * HARD RULE: no libGDX imports in this package.
 */
public interface GameAction {

    /**
     * Executes the action against {@code state}.
     *
     * @param state the current game state (will be mutated)
     * @return an ordered list of {@link GameEvent}s produced by this action;
     *         never {@code null}, may be empty
     * @throws IllegalStateException if the action is not legal in the current state
     */
    List<GameEvent> execute(GameState state);
}
