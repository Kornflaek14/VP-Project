package com.cardgame.logic.events;

/** Fired whenever a player's current or maximum mana changes. */
public record ManaChangedEvent(int playerIndex, int currentMana, int maxMana)
        implements GameEvent {}
