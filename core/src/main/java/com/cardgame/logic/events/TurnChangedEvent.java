package com.cardgame.logic.events;

/** Fired at the start of each new player's turn. */
public record TurnChangedEvent(int newCurrentPlayer, int turnNumber)
        implements GameEvent {}
