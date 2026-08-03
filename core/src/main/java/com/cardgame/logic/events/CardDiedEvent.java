package com.cardgame.logic.events;

import com.cardgame.logic.CardInstance;

/** Fired when a minion's health drops to 0 and it is removed from the board. */
public record CardDiedEvent(int playerIndex, CardInstance card)
        implements GameEvent {}
