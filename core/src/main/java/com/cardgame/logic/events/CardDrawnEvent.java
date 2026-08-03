package com.cardgame.logic.events;

import com.cardgame.logic.CardInstance;

/** Fired when a player draws a card from their deck. */
public record CardDrawnEvent(int playerIndex, CardInstance card)
        implements GameEvent {}
