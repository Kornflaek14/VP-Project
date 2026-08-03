package com.cardgame.logic.events;

import com.cardgame.logic.CardInstance;

/** Fired each time a minion takes damage (used to drive damage-number animations). */
public record DamageDealtEvent(CardInstance target, int amount)
        implements GameEvent {}
