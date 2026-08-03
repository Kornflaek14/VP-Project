package com.cardgame.data;

/**
 * Category of a runtime status effect applied to a {@link com.cardgame.logic.CardInstance}.
 *
 * <ul>
 *   <li>{@link #POISON}    – Takes {@code value} damage at the start of each turn for {@code duration} turns.</li>
 *   <li>{@link #FREEZE}    – Skips attack phase for {@code duration} turns.</li>
 *   <li>{@link #THORNS}    – Melee attackers take {@code value} damage back when hitting this unit.</li>
 *   <li>{@link #HEAL_AURA} – Heals adjacent / all-friendly units {@code value} HP per turn (Ward Doctor).</li>
 * </ul>
 *
 * HARD RULE: no libGDX imports.
 */
public enum StatusEffectType {
    POISON,
    FREEZE,
    THORNS,
    HEAL_AURA
}
