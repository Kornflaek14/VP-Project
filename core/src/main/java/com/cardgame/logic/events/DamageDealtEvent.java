package com.cardgame.logic.events;

import com.cardgame.logic.CardInstance;

/** Fired each time a minion takes damage (used to drive damage-number animations). */
public record DamageDealtEvent(CardInstance source, CardInstance target, int amount, boolean isCombatDamage)
        implements GameEvent {}
