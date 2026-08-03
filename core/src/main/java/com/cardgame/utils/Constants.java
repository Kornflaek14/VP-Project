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
    public static final int MAX_HAND_SIZE           = 10;
    public static final int STARTING_HAND_SIZE      = 4;
    public static final int STARTING_HEALTH         = 30;

    // ── Scale (Health System) ─────────────────────────────────────────────────
    /**
     * The scale tips this many points in either direction to trigger a win.
     * +{@value} = Player 0 wins; -{@value} = Player 1 (opponent) wins.
     * Mirrors Inscryption Act 1 rules.
     */
    public static final int WINNING_SCALE_THRESHOLD = 5;

    // ── Viewport ───────────────────────────────────────────────────────────────
    public static final int VIEWPORT_WIDTH      = 1280;
    public static final int VIEWPORT_HEIGHT     = 720;

    // ── Card actor size (pixels) ───────────────────────────────────────────────
    public static final float CARD_WIDTH        = 90f;
    public static final float CARD_HEIGHT       = 120f;
    public static final float CARD_GAP          = 12f;
}
