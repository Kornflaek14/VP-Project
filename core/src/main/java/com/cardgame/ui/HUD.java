package com.cardgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.cardgame.data.PotionData;
import com.cardgame.data.StatusEffect;
import com.cardgame.logic.GameState;
import com.cardgame.logic.RunManager;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Combat HUD:
 * - Top bar: HP, Gold, Floor info
 * - Bottom-left: Energy orb, Potion slots (3 slots above energy)
 * - Bottom-right: End Turn button
 * - HP bars above player and monster
 * - Monster intent display
 * - Status effect labels beside HP bars
 * - Draw/Discard pile counters
 */
public class HUD extends Group {

    /** Callback interface for when a potion slot is clicked during combat. */
    public interface PotionClickCallback {
        void onPotionClicked(int slotIndex);
    }

    private final BitmapFont font;
    private final BitmapFont largeFont;
    private final BitmapFont smallFont;
    private final BitmapFont tinyFont;
    private final Texture barTexture;
    private final Texture hpBarBgTex;
    private final Texture hpBarFillTex;
    private final Texture hpBarEnemyFillTex;
    private final Texture blockBarTex;
    private final Texture potionSlotTex;
    private final Texture potionSlotEmptyTex;

    // Icon textures
    private Texture energyImage;
    private Texture endTurnImage;
    private Texture heartImage;
    private Texture goldImage;
    private Texture drawImage;
    private Texture discardImage;

    // Potion images per slot (null if no potion loaded)
    private final List<Texture> potionTextures = new ArrayList<>();

    private GameState snapshot;
    private final TextButton endTurnBtn;
    private PotionClickCallback potionCallback;

    // Layout constants
    private static final float PLAYER_X  = 300f;
    private static final float MONSTER_X = 1050f;
    private static final float CHAR_Y    = 280f;
    private static final float HP_BAR_WIDTH  = 180f;
    private static final float HP_BAR_HEIGHT = 18f;

    // Potion slots layout
    private static final float POTION_SLOT_SIZE = 52f;
    private static final float POTION_SLOT_X    = 20f;
    private static final float POTION_BASE_Y    = 285f; // above energy orb

    public HUD(GameState initialState, ChangeListener endTurnListener) {
        this.snapshot = initialState;

        font = new BitmapFont();
        font.getData().setScale(1.2f);

        largeFont = new BitmapFont();
        largeFont.getData().setScale(2.5f);
        largeFont.setColor(Color.WHITE);

        smallFont = new BitmapFont();
        smallFont.getData().setScale(0.9f);

        tinyFont = new BitmapFont();
        tinyFont.getData().setScale(0.75f);

        // Solid-colour background textures
        barTexture        = solidPixel(new Color(0.1f, 0.1f, 0.15f, 0.85f));
        hpBarBgTex        = solidPixel(new Color(0.2f, 0.05f, 0.05f, 0.9f));
        hpBarFillTex      = solidPixel(new Color(0.15f, 0.75f, 0.15f, 1f));
        hpBarEnemyFillTex = solidPixel(new Color(0.85f, 0.15f, 0.15f, 1f));
        blockBarTex       = solidPixel(new Color(0.2f, 0.5f, 0.9f, 0.9f));
        potionSlotTex     = solidPixel(new Color(0.15f, 0.30f, 0.15f, 0.85f));
        potionSlotEmptyTex= solidPixel(new Color(0.1f, 0.1f, 0.12f, 0.60f));

        // Load icon textures
        try { energyImage  = new Texture(Gdx.files.internal("IMAGES/play/energyImage.png")); } catch (Exception e) {}
        try { endTurnImage = new Texture(Gdx.files.internal("IMAGES/play/endTurnImage.png")); } catch (Exception e) {}
        try { heartImage   = new Texture(Gdx.files.internal("IMAGES/play/heart.png")); } catch (Exception e) {}
        try { goldImage    = new Texture(Gdx.files.internal("IMAGES/play/gold.png")); } catch (Exception e) {}
        try { drawImage    = new Texture(Gdx.files.internal("IMAGES/play/drawImage.png")); } catch (Exception e) {}
        try { discardImage = new Texture(Gdx.files.internal("IMAGES/play/discardImage.png")); } catch (Exception e) {}

        // Build potion slot buttons (3 slots)
        buildPotionSlots();

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

    // ── Potion slots ──────────────────────────────────────────

    private void buildPotionSlots() {
        // We use invisible actors as hit-areas for the three potion slots
        for (int i = 0; i < 3; i++) {
            final int slotIndex = i;
            float sx = POTION_SLOT_X;
            float sy = POTION_BASE_Y + i * (POTION_SLOT_SIZE + 6f);
            Actor hitArea = new Actor();
            hitArea.setPosition(sx, sy);
            hitArea.setSize(POTION_SLOT_SIZE, POTION_SLOT_SIZE);
            hitArea.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    if (potionCallback != null) {
                        potionCallback.onPotionClicked(slotIndex);
                    }
                }
            });
            addActor(hitArea);
        }
    }

    /** Set the callback that fires when the player clicks a potion slot. */
    public void setPotionCallback(PotionClickCallback callback) {
        this.potionCallback = callback;
    }

    /** Reload the potion image cache from current RunManager state. */
    private void refreshPotionTextures() {
        // Dispose old
        for (Texture t : potionTextures) {
            if (t != null) t.dispose();
        }
        potionTextures.clear();

        List<PotionData> potions = RunManager.getInstance().getPotions();
        for (PotionData p : potions) {
            Texture tex = null;
            if (p.image() != null && !p.image().isEmpty()) {
                try {
                    if (Gdx.files.internal(p.image()).exists()) {
                        tex = new Texture(Gdx.files.internal(p.image()));
                    }
                } catch (Exception ignored) {}
            }
            potionTextures.add(tex);
        }
    }

    // ── Update ────────────────────────────────────────────────

    public void update(GameState state) {
        this.snapshot = state;
        endTurnBtn.setVisible(state.isPlayerTurn());
        refreshPotionTextures();
    }

    // ── Draw ──────────────────────────────────────────────────

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
        if (heartImage != null) batch.draw(heartImage, 15, h - 42, 28, 28);
        font.setColor(Color.RED);
        font.draw(batch, snapshot.playerHp + "/" + snapshot.playerMaxHp, 50, topY);

        // Top: Gold icon + count
        if (goldImage != null) batch.draw(goldImage, 180, h - 42, 28, 28);
        font.setColor(Color.GOLD);
        font.draw(batch, "" + RunManager.getInstance().getGold(), 215, topY);

        // ── Player HP bar ─────────────────────────────────────
        drawHpBar(batch, parentAlpha,
            PLAYER_X - HP_BAR_WIDTH / 2f, CHAR_Y + 260f,
            HP_BAR_WIDTH, HP_BAR_HEIGHT,
            snapshot.playerHp, snapshot.playerMaxHp,
            hpBarFillTex, snapshot.playerBlock);

        // Player name
        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch,
            RunManager.getInstance().getSelectedCharacter() != null
                ? RunManager.getInstance().getSelectedCharacter().name() : "Player",
            PLAYER_X - 30, CHAR_Y + 300f);

        // Player status effects
        drawStatusEffects(batch, snapshot.playerStatus.summaryString(),
            PLAYER_X - HP_BAR_WIDTH / 2f, CHAR_Y + 245f);

        // ── Monster HP bar ────────────────────────────────────
        drawHpBar(batch, parentAlpha,
            MONSTER_X - HP_BAR_WIDTH / 2f, CHAR_Y + 280f,
            HP_BAR_WIDTH, HP_BAR_HEIGHT,
            snapshot.monsterHp, snapshot.monsterMaxHp,
            hpBarEnemyFillTex, snapshot.monsterBlock);

        // Monster name
        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, snapshot.monsterName, MONSTER_X - 40, CHAR_Y + 320f);

        // Monster status effects
        drawStatusEffects(batch, snapshot.monsterStatus.summaryString(),
            MONSTER_X - HP_BAR_WIDTH / 2f, CHAR_Y + 265f);

        // ── Monster Intent ────────────────────────────────────
        if (snapshot.isPlayerTurn()) {
            float intentY = CHAR_Y + 350f;
            String intentStr;
            Color intentColor;
            if ("ATTACK".equals(snapshot.intentType)) {
                intentStr  = "ATK " + snapshot.intentValue;
                intentColor = new Color(1f, 0.3f, 0.3f, 1f);
            } else {
                intentStr  = "DEF " + snapshot.intentValue;
                intentColor = new Color(0.3f, 0.6f, 1f, 1f);
            }
            font.setColor(intentColor);
            font.draw(batch, intentStr, MONSTER_X - 30, intentY);
        }

        // ── Potion slots (bottom-left, above energy) ──────────
        drawPotionSlots(batch, parentAlpha);

        // ── Energy orb ───────────────────────────────────────
        float energyX = 30f;
        float energyY = 200f;
        float energySize = 70f;
        if (energyImage != null) {
            batch.draw(energyImage, energyX, energyY, energySize, energySize);
        } else {
            batch.setColor(new Color(0.8f, 0.2f, 0.2f, 0.9f));
            batch.draw(barTexture, energyX, energyY, energySize, energySize);
        }
        largeFont.setColor(Color.WHITE);
        String energyStr = snapshot.playerEnergy + "/" + snapshot.playerMaxEnergy;
        largeFont.draw(batch, energyStr, energyX + 8, energyY + energySize / 2f + 12);

        // ── End Turn button background ────────────────────────
        if (endTurnImage != null && snapshot.isPlayerTurn()) {
            batch.setColor(1, 1, 1, parentAlpha);
            batch.draw(endTurnImage, Constants.VIEWPORT_WIDTH - 210, 200, 180, 75);
        }

        // ── Draw pile (bottom-left corner) ───────────────────
        float pileY = 15;
        if (drawImage != null) batch.draw(drawImage, 20, pileY, 50, 50);
        font.setColor(Color.WHITE);
        font.draw(batch, "" + snapshot.drawPile.size(), 75, pileY + 35);

        // ── Discard pile (bottom-right corner) ───────────────
        if (discardImage != null) batch.draw(discardImage, w - 100, pileY, 50, 50);
        font.setColor(Color.WHITE);
        font.draw(batch, "" + snapshot.discardPile.size(), w - 45, pileY + 35);

        batch.setColor(1, 1, 1, 1);
        super.draw(batch, parentAlpha);
    }

    /** Draws 3 potion slots above the energy orb. */
    private void drawPotionSlots(Batch batch, float parentAlpha) {
        List<PotionData> potions = RunManager.getInstance().getPotions();
        int maxSlots = 3;

        for (int i = 0; i < maxSlots; i++) {
            float sx = POTION_SLOT_X;
            float sy = POTION_BASE_Y + i * (POTION_SLOT_SIZE + 6f);

            if (i < potions.size()) {
                // Filled slot
                batch.setColor(1, 1, 1, parentAlpha);
                batch.draw(potionSlotTex, sx, sy, POTION_SLOT_SIZE, POTION_SLOT_SIZE);

                // Potion image or label
                if (i < potionTextures.size() && potionTextures.get(i) != null) {
                    float pad = 4f;
                    batch.draw(potionTextures.get(i),
                        sx + pad, sy + pad,
                        POTION_SLOT_SIZE - pad * 2, POTION_SLOT_SIZE - pad * 2);
                }

                // Show name on hover / always
                tinyFont.setColor(Color.WHITE);
                tinyFont.draw(batch, "" + (i + 1), sx + POTION_SLOT_SIZE + 4, sy + POTION_SLOT_SIZE - 6);
            } else {
                // Empty slot
                batch.setColor(1, 1, 1, 0.4f);
                batch.draw(potionSlotEmptyTex, sx, sy, POTION_SLOT_SIZE, POTION_SLOT_SIZE);
            }
        }

        // Label above potion slots
        tinyFont.setColor(new Color(0.8f, 0.8f, 0.5f, 0.9f));
        tinyFont.draw(batch, "POTIONS", POTION_SLOT_X - 2,
            POTION_BASE_Y + maxSlots * (POTION_SLOT_SIZE + 6f) + 12);
    }

    /** Draw a small coloured status summary string. */
    private void drawStatusEffects(Batch batch, String summary, float x, float y) {
        if (summary == null || summary.isEmpty()) return;
        tinyFont.setColor(new Color(0.9f, 0.7f, 0.2f, 1f));
        tinyFont.draw(batch, summary, x, y);
    }

    /** Draws an HP bar with background, fill, text, and optional block indicator. */
    private void drawHpBar(Batch batch, float alpha,
                           float x, float y, float w, float h,
                           int hp, int maxHp, Texture fillTex, int block) {
        batch.setColor(1, 1, 1, alpha);
        batch.draw(hpBarBgTex, x - 2, y - 2, w + 4, h + 4);

        float fillRatio = Math.max(0, Math.min(1, (float) hp / maxHp));
        batch.draw(fillTex, x, y, w * fillRatio, h);

        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, hp + "/" + maxHp, x + w / 2f - 20, y + h - 2);

        if (block > 0) {
            batch.setColor(1, 1, 1, alpha);
            batch.draw(blockBarTex, x + w + 5, y, 50, h);
            smallFont.setColor(Color.WHITE);
            smallFont.draw(batch, "BLK " + block, x + w + 8, y + h - 2);
        }
    }

    private static Texture solidPixel(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    public float getPlayerX()  { return PLAYER_X;  }
    public float getMonsterX() { return MONSTER_X; }
    public float getCharY()    { return CHAR_Y;     }

    public void disposeResources() {
        font.dispose();
        largeFont.dispose();
        smallFont.dispose();
        tinyFont.dispose();
        barTexture.dispose();
        hpBarBgTex.dispose();
        hpBarFillTex.dispose();
        hpBarEnemyFillTex.dispose();
        blockBarTex.dispose();
        potionSlotTex.dispose();
        potionSlotEmptyTex.dispose();
        if (energyImage  != null) energyImage.dispose();
        if (endTurnImage != null) endTurnImage.dispose();
        if (heartImage   != null) heartImage.dispose();
        if (goldImage    != null) goldImage.dispose();
        if (drawImage    != null) drawImage.dispose();
        if (discardImage != null) discardImage.dispose();
        for (Texture t : potionTextures) if (t != null) t.dispose();
        potionTextures.clear();
    }
}
