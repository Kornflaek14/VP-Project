package com.cardgame.logic;

import com.cardgame.data.StatusEffectType;

/**
 * Mutable runtime tracking of a status effect on a card.
 */
public class ActiveEffect {
    private final StatusEffectType type;
    private final int value;
    private int duration; // -1 for permanent, decrements to 0

    public ActiveEffect(StatusEffectType type, int value, int duration) {
        this.type = type;
        this.value = value;
        this.duration = duration;
    }

    public StatusEffectType getType() { return type; }
    public int getValue() { return value; }
    public int getDuration() { return duration; }

    public void decrementDuration() {
        if (duration > 0) {
            duration--;
        }
    }

    public boolean isExpired() {
        return duration == 0;
    }
}
