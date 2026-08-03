package com.cardgame.logic.events;

import com.cardgame.logic.CardInstance;

public record TokenSpawnedEvent(int ownerIndex, CardInstance token, int boardIndex) implements GameEvent {}
