package com.cardgame.logic.events;

/** Fired when a hero takes direct damage (face damage). */
public record HeroDamagedEvent(int playerIndex, int amount, int newHealth)
        implements GameEvent {}
