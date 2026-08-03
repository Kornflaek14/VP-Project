package com.cardgame.utils;

/**
 * Compile-time constants shared across all packages.
 * <p>
 * HARD RULE: no libGDX imports.
 */
public final class Constants {

    private Constants() {}

    // ── Board ──────────────────────────────────────────────────────────────────
    public static final int MAX_BOARD_SIZE          = 4;
    public static final int MAX_HAND_SIZE           = 6;
    public static final int STARTING_HAND_SIZE      = 6;

    // ── Health ────────────────────────────────────────────────────────────────
    /** Starting hit-points for each player. Game ends when any player's HP ≤ 0. */
    public static final int STARTING_HEALTH         = 10;

    // ── Deck ──────────────────────────────────────────────────────────────────
    /** Number of cards each player starts with in their draw pile. */
    public static final int DECK_SIZE               = 19;

    // ── Viewport ───────────────────────────────────────────────────────────────
    public static final int VIEWPORT_WIDTH      = 1280;
    public static final int VIEWPORT_HEIGHT     = 720;

    // ── Card actor size (pixels) ───────────────────────────────────────────────
    public static final float CARD_WIDTH        = 90f;
    public static final float CARD_HEIGHT       = 120f;
    public static final float CARD_GAP          = 12f;
}
