package com.cardgame.logic.events;

public record MonsterIntentEvent(String intentType, int value) implements GameEvent {}
