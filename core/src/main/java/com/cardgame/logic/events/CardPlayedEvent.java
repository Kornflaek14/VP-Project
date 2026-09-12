package com.cardgame.logic.events;

import com.cardgame.logic.cards.AbstractCard;

public record CardPlayedEvent(AbstractCard card, int damageDealt, int blockGained) implements GameEvent {}
