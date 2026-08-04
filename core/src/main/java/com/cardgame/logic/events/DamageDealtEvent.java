package com.cardgame.logic.events;

public record DamageDealtEvent(String source, String target, int amount) implements GameEvent {}
