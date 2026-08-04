package com.cardgame.logic.events;

public record TurnChangedEvent(int turnNumber, boolean isPlayerTurn) implements GameEvent {}
