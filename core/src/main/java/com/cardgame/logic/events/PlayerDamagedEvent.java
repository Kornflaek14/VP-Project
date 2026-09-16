package com.cardgame.logic.events;

public record PlayerDamagedEvent(int amount, String source) implements GameEvent {
    public PlayerDamagedEvent(int amount) {
        this(amount, "monster");
    }
}
