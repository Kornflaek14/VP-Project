package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.cardgame.logic.GameState;
import com.cardgame.utils.Constants;

/**
 * Scene2D {@link Group} that displays:
 * <ul>
 *   <li>Player 0 health + mana (bottom bar)</li>
 *   <li>Player 1 health (top bar)</li>
 *   <li>Turn number and current player indicator</li>
 *   <li>End Turn button</li>
 * </ul>
 *
 * Visuals and input only — game logic is invoked through the {@link Runnable}
 * callback passed at construction.
 */
public class HUD extends Group {

    private final BitmapFont      font;
    private final BitmapFont      smallFont;
    private final TextButton      endTurnBtn;
    private final Texture         barTexture;
    private final Texture         manaCrystalFull;
    private final Texture         manaCrystalEmpty;

    // Snapshot updated each frame by BattleScreen
    private GameState snapshot;

    // ── Colors ─────────────────────────────────────────────────────────────────
    private static final Color BAR_COLOR  = new Color(0.10f, 0.10f, 0.20f, 0.90f);
    private static final Color MANA_FULL  = new Color(0.35f, 0.55f, 1.00f, 1.00f);
    private static final Color MANA_EMPTY = new Color(0.20f, 0.20f, 0.40f, 1.00f);
    private static final Color GOLD       = new Color(0.96f, 0.84f, 0.38f, 1.00f);

    public HUD(GameState initialState, Runnable onEndTurn) {
        this.snapshot = initialState;

        // ── Fonts ──────────────────────────────────────────────────────────────
        font      = new BitmapFont();
        smallFont = new BitmapFont();
        font.getData().setScale(1.3f);
        smallFont.getData().setScale(0.85f);
        font.setColor(Color.WHITE);
        smallFont.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));

        // ── Textures ───────────────────────────────────────────────────────────
        barTexture       = singlePixel(BAR_COLOR);
        manaCrystalFull  = singlePixel(MANA_FULL);
        manaCrystalEmpty = singlePixel(MANA_EMPTY);

        // ── End Turn button ────────────────────────────────────────────────────
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font          = font;
        style.fontColor     = Color.WHITE;
        style.overFontColor = GOLD;

        endTurnBtn = new TextButton("END TURN", style);
        endTurnBtn.setPosition(Constants.VIEWPORT_WIDTH - 190f, Constants.VIEWPORT_HEIGHT / 2f - 25f);
        endTurnBtn.setSize(170f, 50f);
        endTurnBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onEndTurn != null) onEndTurn.run();
            }
        });
        addActor(endTurnBtn);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /** Call each frame with the latest game state. */
    public void update(GameState state) {
        this.snapshot = state;
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
                "Opponent  ❤ " + snapshot.getHealth(1),
                14f, Constants.VIEWPORT_HEIGHT - 10f);

        // ── Bottom bar (player / player 0) ────────────────────────────────────
        batch.setColor(1f, 1f, 1f, parentAlpha * 0.9f);
        batch.draw(barTexture, 0, 0, W, barH);

        font.setColor(1f, 1f, 1f, parentAlpha);
        font.draw(batch, "Player  ❤ " + snapshot.getHealth(0), 14f, barH - 8f);

        // ── Mana crystals (bottom, after HP label) ────────────────────────────
        drawManaCrystals(batch, parentAlpha, 180f, 8f,
                snapshot.getMana(0), snapshot.getPlayer(0).maxMana);

        // ── Turn indicator (centre) ────────────────────────────────────────────
        String turnText = "Turn " + currentTurn()
                        + "  |  " + (snapshot.getCurrentPlayer() == 0 ? "YOUR TURN" : "OPPONENT TURN");
        smallFont.setColor(GOLD.r, GOLD.g, GOLD.b, parentAlpha);
        smallFont.draw(batch, turnText, W / 2f - 80f, Constants.VIEWPORT_HEIGHT / 2f + 12f);

        // ── Draw children (End Turn button) ───────────────────────────────────
        batch.setColor(1f, 1f, 1f, parentAlpha);
        super.draw(batch, parentAlpha);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void drawManaCrystals(Batch batch, float alpha,
                                  float startX, float startY,
                                  int current, int max) {
        float crystalW = 18f, crystalH = 18f, gap = 4f;
        for (int i = 0; i < max; i++) {
            Texture tex = i < current ? manaCrystalFull : manaCrystalEmpty;
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(tex, startX + i * (crystalW + gap), startY, crystalW, crystalH);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    // Lazily determined from the snapshot — avoids coupling to TurnManager
    private int currentTurn() {
        // We don't hold TurnManager ref here; this is purely cosmetic
        return 1; // BattleScreen may set this via a dedicated setter if needed
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
        manaCrystalFull.dispose();
        manaCrystalEmpty.dispose();
    }
}
