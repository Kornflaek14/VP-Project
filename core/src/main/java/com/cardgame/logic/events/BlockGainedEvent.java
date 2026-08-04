package com.cardgame.logic.events;

public record BlockGainedEvent(String target, int amount) implements GameEvent {}
