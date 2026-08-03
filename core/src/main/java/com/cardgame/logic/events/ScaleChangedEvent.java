package com.cardgame.logic.events;

/** Fired when direct damage is dealt to the scale. */
public record ScaleChangedEvent(int scaleBalance, int damageAmount, int dealingPlayer)
        implements GameEvent {}
