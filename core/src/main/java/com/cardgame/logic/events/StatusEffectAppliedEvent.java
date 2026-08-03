package com.cardgame.logic.events;

import com.cardgame.data.StatusEffectType;
import com.cardgame.logic.CardInstance;

public record StatusEffectAppliedEvent(CardInstance card, StatusEffectType type, int value, int duration) implements GameEvent {}
