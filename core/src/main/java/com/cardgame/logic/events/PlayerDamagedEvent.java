package com.cardgame.logic.events;

/** Fired when a player takes direct damage (overflow or empty-lane attack). */
public record PlayerDamagedEvent(int playerIndex, int amount) implements GameEvent {}
