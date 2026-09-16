package com.cardgame.logic.events;

public record PlayerDefendedEvent(int amountBlocked) implements GameEvent {}
