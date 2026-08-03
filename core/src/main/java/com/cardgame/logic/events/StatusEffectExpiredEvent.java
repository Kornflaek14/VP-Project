package com.cardgame.logic.events;

import com.cardgame.data.StatusEffectType;
import com.cardgame.logic.CardInstance;

public record StatusEffectExpiredEvent(CardInstance card, StatusEffectType type) implements GameEvent {}
