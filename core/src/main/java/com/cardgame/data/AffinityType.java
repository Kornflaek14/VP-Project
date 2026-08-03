package com.cardgame.data;

/**
 * Elemental / thematic affinity — the "Scissor" counter system.
 *
 * <p>Counter table (attacker → defender → damage multiplier):
 * <pre>
 *  BLADE    vs CHEMICAL  → ×1.5  (blade punctures gas canister)
 *  BLADE    vs MEDICAL   → ×1.0  (no interaction)
 *  CHEMICAL vs BLUNT     → ×1.5  (gas overwhelms unprotected orderlies)
 *  CHEMICAL vs MEDICAL   → ×0.5  (mask blocks gas)
 *  BLUNT    vs MEDICAL   → ×1.5  (physical overpowers protective gear)
 *  BLUNT    vs CHEMICAL  → ×0.5  (no reach vs gas)
 *  MEDICAL  vs CHEMICAL  → ×1.5  (medical training counters chemicals)
 *  ELECTRIC vs BLADE     → ×1.5  (metal conducts electricity)
 *  BLADE    vs ELECTRIC  → ×0.5  (cut metal = shock)
 *  NEUTRAL  → ×1.0 in all matchups
 * </pre>
 *
 * HARD RULE: no libGDX imports.
 */
public enum AffinityType {
    NEUTRAL,
    BLADE,
    CHEMICAL,
    MEDICAL,
    BLUNT,
    ELECTRIC
}
