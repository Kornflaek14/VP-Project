package com.cardgame.utils;

/**
 * Compile-time constants shared across all packages.
 */
public final class Constants {

    private Constants() {}

    // ── Viewport ───────────────────────────────────────────────
    public static final int VIEWPORT_WIDTH  = 1440;
    public static final int VIEWPORT_HEIGHT = 900;

    // ── Card actor size (pixels) ───────────────────────────────
    public static final float CARD_WIDTH  = 130f;
    public static final float CARD_HEIGHT = 180f;
    public static final float CARD_GAP    = 10f;

    // ── Hand ───────────────────────────────────────────────────
    public static final int MAX_HAND_SIZE = 10;

    // ── Combat ─────────────────────────────────────────────────
    public static final int CARDS_PER_TURN = 5;
}
