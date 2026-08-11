package com.cardgame.logic.events;

import com.cardgame.logic.cards.AbstractCard;

public record CardDrawnEvent(AbstractCard card) implements GameEvent {}
