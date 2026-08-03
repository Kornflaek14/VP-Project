package com.cardgame.logic.events;

import com.cardgame.logic.CardInstance;

/** Fired at the start of a combat exchange between two minions. */
public record CardAttackedEvent(CardInstance attacker, CardInstance defender)
        implements GameEvent {}
