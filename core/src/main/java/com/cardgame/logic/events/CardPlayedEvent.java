package com.cardgame.logic.events;

import com.cardgame.logic.CardInstance;

/** Fired when a minion is successfully played from hand onto the board. */
public record CardPlayedEvent(int playerIndex, CardInstance card, int boardPosition)
        implements GameEvent {}
