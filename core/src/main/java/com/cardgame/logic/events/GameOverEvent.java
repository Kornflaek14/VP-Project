package com.cardgame.logic.events;

/** winnerIndex: 0 = player wins, 1 = monster wins. */
public record GameOverEvent(int winnerIndex) implements GameEvent {}
