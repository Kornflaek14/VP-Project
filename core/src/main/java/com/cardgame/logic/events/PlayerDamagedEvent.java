package com.cardgame.logic.events;

public record PlayerDamagedEvent(int amount) implements GameEvent {}
