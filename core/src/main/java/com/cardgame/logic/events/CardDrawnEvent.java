package com.cardgame.logic.events;

import com.cardgame.data.CardData;

public record CardDrawnEvent(CardData card) implements GameEvent {}
