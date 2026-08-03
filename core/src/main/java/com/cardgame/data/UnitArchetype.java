package com.cardgame.data;

/**
 * Archetype of a {@link CardType#UNIT} card — affects board mechanics.
 *
 * <ul>
 *   <li>{@link #STANDARD}      – Normal unit; attacks and takes retaliation.</li>
 *   <li>{@link #TAUNT}         – Opponent must attack this unit before others.</li>
 *   <li>{@link #RANGED}        – Attacks without taking retaliation damage.</li>
 *   <li>{@link #ARTILLERY}     – Like Ranged but may target the enemy hero directly
 *                                 even when enemy board cards are present.</li>
 *   <li>{@link #TOKEN_SPAWNER} – Produces a weak token unit in an adjacent slot each turn.</li>
 * </ul>
 *
 * HARD RULE: no libGDX imports.
 */
public enum UnitArchetype {
    STANDARD,
    TAUNT,
    RANGED,
    ARTILLERY,
    TOKEN_SPAWNER
}
