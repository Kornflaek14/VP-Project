package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cardgame.logic.GameState;
import com.cardgame.utils.Constants;

/**
 * Scene2D {@link Group} that displays:
 * <ul>
 *   <li>Player 0 HP bar (bottom)</li>
 *   <li>Player 1 HP bar (top)</li>
 *   <li>Bone count, sacrifice credit</li>
 *   <li>Turn number and current player indicator</li>
 *   <li>Status message</li>
 * </ul>
 */
public class HUD extends Group {

    private final BitmapFont      font;
    private final BitmapFont      smallFont;
    private final Texture         barTexture;
    private final Texture         hpSegTex;

    // Snapshot updated each frame by BattleScreen
    private GameState snapshot;
    private int       turnNumber = 1;

    // ── Colors ─────────────────────────────────────────────────────────────────
    private static final Color BAR_COLOR  = new Color(0.10f, 0.10f, 0.20f, 0.90f);
    private static final Color GOLD       = new Color(0.96f, 0.84f, 0.38f, 1.00f);
    
    // HP bar gradient
    private static final Color HP_GREEN   = new Color(0.20f, 0.82f, 0.40f, 1.00f);
    private static final Color HP_YELLOW  = new Color(0.92f, 0.82f, 0.20f, 1.00f);
    private static final Color HP_RED     = new Color(0.90f, 0.22f, 0.18f, 1.00f);
    private static final Color HP_BG      = new Color(0.15f, 0.15f, 0.22f, 0.80f);
    
    private String inputModeMessage = "DRAG A CARD TO PLAY";

    public HUD(GameState initialState) {
        this.snapshot = initialState;

        // ── Fonts ──────────────────────────────────────────────────────────────
        font      = new BitmapFont();
        smallFont = new BitmapFont();
        font.getData().setScale(1.3f);
        smallFont.getData().setScale(0.85f);
        font.setColor(Color.WHITE);
        smallFont.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));

        // ── Textures ───────────────────────────────────────────────────────────
        barTexture = singlePixel(BAR_COLOR);
        hpSegTex   = singlePixel(Color.WHITE); // tinted at draw time
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /** Call each frame with the latest game state. */
    public void update(GameState state) {
        this.snapshot = state;
    }

    /** Sets the turn number displayed in the centre turn indicator. */
    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }
    
    public void setInputModeMessage(String msg) {
        this.inputModeMessage = msg;
    }

    // ── Draw ───────────────────────────────────────────────────────────────────

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (snapshot == null) { super.draw(batch, parentAlpha); return; }

        float W = Constants.VIEWPORT_WIDTH;
        float barH = 44f;

        // ── Top bar (opponent / player 1) ──────────────────────────────────────
        batch.setColor(1f, 1f, 1f, parentAlpha * 0.9f);
        batch.draw(barTexture, 0, Constants.VIEWPORT_HEIGHT - barH, W, barH);

        font.setColor(1f, 1f, 1f, parentAlpha);
        font.draw(batch,
                "Leshy",
                14f, Constants.VIEWPORT_HEIGHT - 10f);
        
        // ── Scale Indicator (Win Condition) ────────────────────────────────────
        drawScale(batch, parentAlpha, W / 2f, Constants.VIEWPORT_HEIGHT / 2f + 50f);

        // ── Bottom bar (player / player 0) ────────────────────────────────────
        batch.setColor(1f, 1f, 1f, parentAlpha * 0.9f);
        batch.draw(barTexture, 0, 0, W, barH);

        font.setColor(1f, 1f, 1f, parentAlpha);
        font.draw(batch, "Bones: " + snapshot.getBones(0) + " | Blood: " + snapshot.getPlayer(0).sacrificeCredit, 14f, barH - 8f);

        // ── Turn indicator (centre) ────────────────────────────────────────────
        String turnText = "Turn " + turnNumber
                        + "  |  " + (snapshot.getCurrentPlayer() == 0 ? "YOUR TURN" : "OPPONENT TURN");
        smallFont.setColor(GOLD.r, GOLD.g, GOLD.b, parentAlpha);
        smallFont.draw(batch, turnText, W / 2f - 80f, Constants.VIEWPORT_HEIGHT / 2f + 12f);
        
        // ── Input Mode Message (bottom center) ─────────────────────────────────
        smallFont.setColor(0.7f, 0.9f, 1.0f, parentAlpha);
        smallFont.draw(batch, inputModeMessage, W / 2f - 60f, barH - 12f);

        // ── Draw children ─────────────────────────────────────────────────────
        batch.setColor(1f, 1f, 1f, parentAlpha);
        super.draw(batch, parentAlpha);
    }

    private void drawScale(Batch batch, float parentAlpha, float cx, float cy) {
        int balance = snapshot.getScaleBalance();
        
        String leftLabel = "LESHY";
        String rightLabel = "YOU";
        
        smallFont.setColor(HP_RED.r, HP_RED.g, HP_RED.b, parentAlpha);
        smallFont.draw(batch, leftLabel, cx - 150f, cy + 6f);
        
        smallFont.setColor(HP_GREEN.r, HP_GREEN.g, HP_GREEN.b, parentAlpha);
        smallFont.draw(batch, rightLabel, cx + 110f, cy + 6f);
        
        // Draw 11 segments
        float segW = 16f;
        float segH = 16f;
        float gap = 4f;
        float totalW = 11 * (segW + gap) - gap;
        float startX = cx - totalW / 2f;
        
        for (int i = -5; i <= 5; i++) {
            float x = startX + (i + 5) * (segW + gap);
            if (i == 0) {
                batch.setColor(0.5f, 0.5f, 0.5f, parentAlpha); // Center neutral
            } else if (i < 0) {
                // Leshy side
                if (balance <= i) {
                    batch.setColor(HP_RED.r, HP_RED.g, HP_RED.b, parentAlpha);
                } else {
                    batch.setColor(0.2f, 0.1f, 0.1f, parentAlpha * 0.5f);
                }
            } else {
                // Player side
                if (balance >= i) {
                    batch.setColor(HP_GREEN.r, HP_GREEN.g, HP_GREEN.b, parentAlpha);
                } else {
                    batch.setColor(0.1f, 0.2f, 0.1f, parentAlpha * 0.5f);
                }
            }
            batch.draw(hpSegTex, x, cy - segH / 2f, segW, segH);
        }
        
        // Center text
        font.setColor(GOLD.r, GOLD.g, GOLD.b, parentAlpha);
        String valText = (balance > 0) ? "+" + balance : (balance < 0) ? String.valueOf(balance) : "0";
        font.draw(batch, valText, cx - 12f, cy - 18f);
    }

    private static Texture singlePixel(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    // ── Disposal ───────────────────────────────────────────────────────────────

    public void disposeResources() {
        font.dispose();
        smallFont.dispose();
        barTexture.dispose();
        hpSegTex.dispose();
    }
}
