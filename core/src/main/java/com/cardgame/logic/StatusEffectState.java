package com.cardgame.logic;

import com.cardgame.data.StatusEffect;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tracks status effect stacks for a single combatant (player or monster).
 * Stacks are integer counts; 0 means the effect is inactive.
 */
public final class StatusEffectState {

    private final Map<StatusEffect, Integer> stacks = new EnumMap<>(StatusEffect.class);

    /** Apply N stacks of the given effect (adds to existing). */
    public void apply(StatusEffect effect, int amount) {
        if (amount <= 0) return;
        stacks.merge(effect, amount, Integer::sum);
    }

    /** Get current stacks (0 if none). */
    public int get(StatusEffect effect) {
        return stacks.getOrDefault(effect, 0);
    }

    /** Returns true if the effect has at least 1 stack. */
    public boolean has(StatusEffect effect) {
        return get(effect) > 0;
    }

    /**
     * Decrements all duration-based effects by 1 at turn start.
     * Poison is handled separately (deals damage, then decrements).
     */
    public void tickDurationEffects() {
        for (StatusEffect e : new StatusEffect[]{
                StatusEffect.VULNERABLE, StatusEffect.WEAK}) {
            int current = get(e);
            if (current > 0) {
                if (current == 1) stacks.remove(e);
                else stacks.put(e, current - 1);
            }
        }
    }

    /**
     * Tick Poison: returns the damage dealt and decrements.
     */
    public int tickPoison() {
        int poisonStacks = get(StatusEffect.POISON);
        if (poisonStacks > 0) {
            if (poisonStacks == 1) stacks.remove(StatusEffect.POISON);
            else stacks.put(StatusEffect.POISON, poisonStacks - 1);
        }
        return poisonStacks; // HP to lose
    }

    /** Remove all stacks of all effects (end of combat). */
    public void clear() {
        stacks.clear();
    }

    /** Returns a human-readable summary for HUD display. */
    public String summaryString() {
        if (stacks.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<StatusEffect, Integer> entry : stacks.entrySet()) {
            if (entry.getValue() > 0) {
                sb.append(abbreviation(entry.getKey())).append(":").append(entry.getValue()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private static String abbreviation(StatusEffect e) {
        switch (e) {
            case VULNERABLE: return "VUL";
            case WEAK:       return "WEK";
            case STRENGTH:   return "STR";
            case DEXTERITY:  return "DEX";
            case POISON:     return "PSN";
            default:         return e.name();
        }
    }
}
