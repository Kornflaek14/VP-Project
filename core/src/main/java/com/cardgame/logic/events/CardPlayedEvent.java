package com.cardgame.logic.events;

import com.cardgame.data.CardData;

public record CardPlayedEvent(CardData card, int damageDealt, int blockGained) implements GameEvent {}
