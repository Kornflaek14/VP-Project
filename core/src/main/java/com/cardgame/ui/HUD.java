package com.cardgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.cardgame.logic.GameState;
import com.cardgame.utils.Constants;

/**
 * Combat HUD matching a Slay-the-Spire visual layout:
 * - Top bar: HP, Gold, Floor info with icons
 * - Bottom-left: Energy orb
 * - Bottom-right: End Turn button
 * - HP bars above player and monster positions
 * - Monster intent display
 * - Draw/Discard pile counters
 */
public class HUD extends Group {

    private final BitmapFont font;
    private final BitmapFont largeFont;
    private final BitmapFont smallFont;
    private final Texture barTexture;
    private final Texture hpBarBgTex;
    private final Texture hpBarFillTex;
    private final Texture hpBarEnemyFillTex;
    private final Texture blockBarTex;

    // Icon textures
    private Texture energyImage;
    private Texture endTurnImage;
    private Texture heartImage;
    private Texture goldImage;
    private Texture drawImage;
    private Texture discardImage;

    private GameState snapshot;
    private final TextButton endTurnBtn;

    // Layout constants matching the reference image
    private static final float PLAYER_X = 300f;     // player center X
    private static final float MONSTER_X = 1050f;    // monster center X
    private static final float CHAR_Y = 280f;        // character base Y
    private static final float HP_BAR_WIDTH = 180f;
    private static final float HP_BAR_HEIGHT = 18f;

    public HUD(GameState initialState, ChangeListener endTurnListener) {
        this.snapshot = initialState;

        font = new BitmapFont();
        font.getData().setScale(1.2f);

        largeFont = new BitmapFont();
        largeFont.getData().setScale(2.5f);
        largeFont.setColor(Color.WHITE);

        smallFont = new BitmapFont();
        smallFont.getData().setScale(0.9f);

        // Background textures
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.1f, 0.1f, 0.15f, 0.85f));
        pm.fill();
        barTexture = new Texture(pm);
        pm.dispose();

        pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.2f, 0.05f, 0.05f, 0.9f));
        pm.fill();
        hpBarBgTex = new Texture(pm);
        pm.dispose();

        pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.15f, 0.75f, 0.15f, 1f));
        pm.fill();
        hpBarFillTex = new Texture(pm);
        pm.dispose();

        pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.85f, 0.15f, 0.15f, 1f));
        pm.fill();
        hpBarEnemyFillTex = new Texture(pm);
        pm.dispose();

        pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.2f, 0.5f, 0.9f, 0.9f));
        pm.fill();
        blockBarTex = new Texture(pm);
        pm.dispose();

        // Load icon textures
        try { energyImage  = new Texture(Gdx.files.internal("IMAGES/play/energyImage.png")); } catch (Exception e) {}
        try { endTurnImage = new Texture(Gdx.files.internal("IMAGES/play/endTurnImage.png")); } catch (Exception e) {}
        try { heartImage   = new Texture(Gdx.files.internal("IMAGES/play/heart.png")); } catch (Exception e) {}
        try { goldImage    = new Texture(Gdx.files.internal("IMAGES/play/gold.png")); } catch (Exception e) {}
        try { drawImage    = new Texture(Gdx.files.internal("IMAGES/play/drawImage.png")); } catch (Exception e) {}
        try { discardImage = new Texture(Gdx.files.internal("IMAGES/play/discardImage.png")); } catch (Exception e) {}

        // End Turn button
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = Color.YELLOW;

        endTurnBtn = new TextButton("", btnStyle);
        endTurnBtn.setSize(160, 55);
        endTurnBtn.setPosition(Constants.VIEWPORT_WIDTH - 200, 210);
        endTurnBtn.addListener(endTurnListener);
        addActor(endTurnBtn);
    }

    public void update(GameState state) {
        this.snapshot = state;
        endTurnBtn.setVisible(state.isPlayerTurn());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (snapshot == null) { super.draw(batch, parentAlpha); return; }

        float w = Constants.VIEWPORT_WIDTH;
        float h = Constants.VIEWPORT_HEIGHT;

        // ── Top bar background ────────────────────────────────
        batch.setColor(1, 1, 1, parentAlpha);
        batch.draw(barTexture, 0, h - 50, w, 50);

        // Top-left: Heart icon + HP
        float topY = h - 15;
        if (heartImage != null) {
            batch.draw(heartImage, 15, h - 42, 28, 28);
        }
        font.setColor(Color.RED);
        font.draw(batch, snapshot.playerHp + "/" + snapshot.playerMaxHp, 50, topY);

        // Top: Gold icon + count
        if (goldImage != null) {
            batch.draw(goldImage, 180, h - 42, 28, 28);
        }
        font.setColor(Color.GOLD);
        font.draw(batch, "" + com.cardgame.logic.RunManager.getInstance().getGold(), 215, topY);

        // ── Player HP bar (above player character) ────────────
        drawHpBar(batch, parentAlpha,
            PLAYER_X - HP_BAR_WIDTH / 2f, CHAR_Y + 260f,
            HP_BAR_WIDTH, HP_BAR_HEIGHT,
            snapshot.playerHp, snapshot.playerMaxHp,
            hpBarFillTex, snapshot.playerBlock);

        // Player name
        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, com.cardgame.logic.RunManager.getInstance().getSelectedCharacter() != null
            ? com.cardgame.logic.RunManager.getInstance().getSelectedCharacter().name() : "Player",
            PLAYER_X - 30, CHAR_Y + 300f);

        // ── Monster HP bar (above monster) ────────────────────
        drawHpBar(batch, parentAlpha,
            MONSTER_X - HP_BAR_WIDTH / 2f, CHAR_Y + 280f,
            HP_BAR_WIDTH, HP_BAR_HEIGHT,
            snapshot.monsterHp, snapshot.monsterMaxHp,
            hpBarEnemyFillTex, snapshot.monsterBlock);

        // Monster name
        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, snapshot.monsterName, MONSTER_X - 40, CHAR_Y + 320f);

        // ── Monster Intent (above monster, above HP bar) ──────
        if (snapshot.isPlayerTurn()) {
            float intentY = CHAR_Y + 350f;
            String intentStr;
            Color intentColor;
            if ("ATTACK".equals(snapshot.intentType)) {
                intentStr = "ATK " + snapshot.intentValue;
                intentColor = new Color(1f, 0.3f, 0.3f, 1f);
            } else {
                intentStr = "DEF " + snapshot.intentValue;
                intentColor = new Color(0.3f, 0.6f, 1f, 1f);
            }
            font.setColor(intentColor);
            font.draw(batch, intentStr, MONSTER_X - 30, intentY);
        }

        // ── Energy orb (bottom-left) ──────────────────────────
        float energyX = 30;
        float energyY = 200;
        float energySize = 70;
        if (energyImage != null) {
            batch.draw(energyImage, energyX, energyY, energySize, energySize);
        } else {
            // Fallback: draw a circle-like shape
            batch.setColor(new Color(0.8f, 0.2f, 0.2f, 0.9f));
            batch.draw(barTexture, energyX, energyY, energySize, energySize);
        }
        // Energy text centered on orb
        largeFont.setColor(Color.WHITE);
        String energyStr = snapshot.playerEnergy + "/" + snapshot.playerMaxEnergy;
        largeFont.draw(batch, energyStr, energyX + 8, energyY + energySize / 2f + 12);

        // ── End Turn button background ────────────────────────
        if (endTurnImage != null && snapshot.isPlayerTurn()) {
            batch.setColor(1, 1, 1, parentAlpha);
            batch.draw(endTurnImage,
                Constants.VIEWPORT_WIDTH - 210, 200, 180, 75);
        }

        // ── Draw pile (bottom-left corner) ────────────────────
        float pileY = 15;
        if (drawImage != null) {
            batch.draw(drawImage, 20, pileY, 50, 50);
        }
        font.setColor(Color.WHITE);
        font.draw(batch, "" + snapshot.drawPile.size(), 75, pileY + 35);

        // ── Discard pile (bottom-right corner) ────────────────
        if (discardImage != null) {
            batch.draw(discardImage, w - 100, pileY, 50, 50);
        }
        font.setColor(Color.WHITE);
        font.draw(batch, "" + snapshot.discardPile.size(), w - 45, pileY + 35);

        batch.setColor(1, 1, 1, 1);
        super.draw(batch, parentAlpha);
    }

    /**
     * Draws an HP bar with background, fill, text, and optional block indicator.
     */
    private void drawHpBar(Batch batch, float alpha,
                           float x, float y, float w, float h,
                           int hp, int maxHp, Texture fillTex, int block) {
        // Background
        batch.setColor(1, 1, 1, alpha);
        batch.draw(hpBarBgTex, x - 2, y - 2, w + 4, h + 4);

        // HP fill
        float fillRatio = Math.max(0, Math.min(1, (float) hp / maxHp));
        batch.draw(fillTex, x, y, w * fillRatio, h);

        // HP text centered
        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, hp + "/" + maxHp, x + w / 2f - 20, y + h - 2);

        // Block indicator (shield icon above bar)
        if (block > 0) {
            batch.setColor(1, 1, 1, alpha);
            batch.draw(blockBarTex, x + w + 5, y, 50, h);
            smallFont.setColor(Color.WHITE);
            smallFont.draw(batch, "BLK " + block, x + w + 8, y + h - 2);
        }
    }

    public float getPlayerX() { return PLAYER_X; }
    public float getMonsterX() { return MONSTER_X; }
    public float getCharY() { return CHAR_Y; }

    public void disposeResources() {
        font.dispose();
        largeFont.dispose();
        smallFont.dispose();
        barTexture.dispose();
        hpBarBgTex.dispose();
        hpBarFillTex.dispose();
        hpBarEnemyFillTex.dispose();
        blockBarTex.dispose();
        if (energyImage  != null) energyImage.dispose();
        if (endTurnImage != null) endTurnImage.dispose();
        if (heartImage   != null) heartImage.dispose();
        if (goldImage    != null) goldImage.dispose();
        if (drawImage    != null) drawImage.dispose();
        if (discardImage != null) discardImage.dispose();
    }
}
