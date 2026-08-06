package com.cardgame.data;

/**
 * All status effects that can be applied to player or monster.
 * Stacks are tracked in StatusEffectState.
 */
public enum StatusEffect {
    /** Takes 50% more damage for N turns. */
    VULNERABLE,
    /** Deals 25% less damage for N turns. */
    WEAK,
    /** Deal +N damage per attack. */
    STRENGTH,
    /** Gain +N block from block cards. */
    DEXTERITY,
    /** Lose N HP at turn start, then decrement. */
    POISON
}
