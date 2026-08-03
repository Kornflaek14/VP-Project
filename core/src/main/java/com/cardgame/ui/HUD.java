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

    // Snapshot updated each frame by BattleScreen
    private GameState snapshot;
    private int       turnNumber = 1;

    // ── Colors ─────────────────────────────────────────────────────────────────
    private static final Color BAR_COLOR  = new Color(0.10f, 0.10f, 0.20f, 0.90f);
    private static final Color GOLD       = new Color(0.96f, 0.84f, 0.38f, 1.00f);
    
    private String inputModeMessage = "SELECT A CARD";

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
                if (snapshot != null && snapshot.getCurrentPlayer() == 0 && onEndTurn != null) {
                    onEndTurn.run();
                }
            }
        });
        addActor(endTurnBtn);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /** Call each frame with the latest game state and current turn number. */
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
                "Opponent Bones: " + snapshot.getBones(1),
                14f, Constants.VIEWPORT_HEIGHT - 10f);
        
        // ── Top Center (Scale) ────────────────────────────────────────────────
        int balance = snapshot.getScaleBalance();
        String scaleText = "SCALE: " + balance + " / ±" + com.cardgame.utils.Constants.WINNING_SCALE_THRESHOLD;
        if (balance > 0)      scaleText += "  ▶ Winning";
        else if (balance < 0) scaleText += "  ◀ Losing";
        font.draw(batch, scaleText, W / 2f - 100f, Constants.VIEWPORT_HEIGHT - 10f);

        // ── Bottom bar (player / player 0) ────────────────────────────────────
        batch.setColor(1f, 1f, 1f, parentAlpha * 0.9f);
        batch.draw(barTexture, 0, 0, W, barH);

        font.setColor(1f, 1f, 1f, parentAlpha);
        font.draw(batch, "Bones: " + snapshot.getBones(0) + " | Blood: " + snapshot.getPlayer(0).sacrificeCredit, 14f, barH - 8f);

        // ── Turn indicator (centre) ────────────────────────────────────────────
        String turnText = "Turn " + currentTurn()
                        + "  |  " + (snapshot.getCurrentPlayer() == 0 ? "YOUR TURN" : "OPPONENT TURN");
        smallFont.setColor(GOLD.r, GOLD.g, GOLD.b, parentAlpha);
        smallFont.draw(batch, turnText, W / 2f - 80f, Constants.VIEWPORT_HEIGHT / 2f + 12f);
        
        // ── Input Mode Message (bottom center) ─────────────────────────────────
        smallFont.setColor(0.7f, 0.9f, 1.0f, parentAlpha);
        smallFont.draw(batch, inputModeMessage, W / 2f - 60f, barH - 12f);

        // ── Draw children (End Turn button) ───────────────────────────────────
        batch.setColor(1f, 1f, 1f, parentAlpha);
        super.draw(batch, parentAlpha);
    }



    // Reads the turn number supplied by BattleScreen via setTurnNumber()
    private int currentTurn() {
        return turnNumber;
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
    }
}
