package com.cardgame.logic.events;

/** Fired when a player's hero health reaches 0. {@code winnerIndex} is 0 or 1. */
public record GameOverEvent(int winnerIndex) implements GameEvent {}
