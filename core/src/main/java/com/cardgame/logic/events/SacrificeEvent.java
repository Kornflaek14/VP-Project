package com.cardgame.logic.events;

import com.cardgame.logic.CardInstance;

public record SacrificeEvent(int playerIndex, CardInstance sacrificedCard) implements GameEvent {}
