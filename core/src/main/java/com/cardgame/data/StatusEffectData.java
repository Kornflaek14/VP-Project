package com.cardgame.data;

/**
 * Pure data class representing a status effect embedded in a card template
 * or applied at runtime.
 *
 * HARD RULE: no libGDX imports.
 */
public final class StatusEffectData {

    private final StatusEffectType type;
    private final int value;
    private final int duration; // -1 for permanent

    // No-arg constructor for Gson
    public StatusEffectData() {
        this(StatusEffectType.POISON, 0, 0);
    }

    public StatusEffectData(StatusEffectType type, int value, int duration) {
        this.type     = type;
        this.value    = value;
        this.duration = duration;
    }

    public StatusEffectType type()     { return type; }
    public int              value()    { return value; }
    public int              duration() { return duration; }

    @Override
    public String toString() {
        return String.format("StatusEffect[%s val=%d dur=%d]", type, value, duration);
    }
}
