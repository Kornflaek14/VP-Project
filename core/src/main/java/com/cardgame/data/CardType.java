package com.cardgame.data;

/**
 * Top-level category of a card.
 *
 * <ul>
 *   <li>{@link #UNIT}       – Persistent creature placed on the board.</li>
 *   <li>{@link #INSTRUMENT} – One-shot melee/blade damage spell (goes to discard after use).</li>
 *   <li>{@link #CHEMICAL}   – One-shot gas/poison effect (AoE or targeted).</li>
 *   <li>{@link #ITEM}       – Buff item (Syringe, Pill) applied to the board immediately.</li>
 *   <li>{@link #SACRIFICE}  – Fodder card (Blood Bag); destroyed to pay sacrifice costs.</li>
 *   <li>{@link #TRAP}       – Played face-down; triggers on enemy action (stub for now).</li>
 * </ul>
 *
 * HARD RULE: no libGDX imports.
 */
public enum CardType {
    UNIT,
    INSTRUMENT,
    CHEMICAL,
    ITEM,
    SACRIFICE,
    TRAP
}
