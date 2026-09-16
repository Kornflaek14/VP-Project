package com.cardgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.cardgame.logic.potions.AbstractPotion;
import com.cardgame.logic.GameState;
import com.cardgame.logic.RunManager;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Combat HUD:
 * - Top bar: HP, Gold, Floor info
 * - Bottom-left: Energy orb
 * - Header: Potion holsters, deck and map navigation
 * - Bottom-right: End Turn button
 * - Player HP bar and status effects below the character
 * - Monster intent display
 * - Status effect labels beside HP bars
 * - Draw/Discard pile counters
 */
public class HUD extends Group {

    /** Callback interface for when a potion slot is clicked during combat. */
    public interface PotionClickCallback {
        void onPotionClicked(int slotIndex);
    }

    public interface PileClickCallback {
        void onDrawPileClicked();
        void onDiscardPileClicked();
    }

    public interface NavigationCallback {
        void onDeckClicked();
        void onMapClicked();
    }

    private final BitmapFont font;
    private final BitmapFont largeFont;
    private final BitmapFont smallFont;
    private final BitmapFont tinyFont;
    private final CombatUiAssets art;
    private final Texture barTexture;
    private final CombatHealthBar healthBar;
    private final TextTooltip.TextTooltipStyle tooltipStyle;
    private final List<TextTooltip> potionTooltips = new ArrayList<>();

    // Potion images per slot (null if no potion loaded)
    private final List<Texture> potionTextures = new ArrayList<>();

    private GameState snapshot;
    private final TextButton endTurnBtn;
    private PotionClickCallback potionCallback;
    private PileClickCallback pileCallback;
    private NavigationCallback navigationCallback;

    // Layout constants
    private static final float PLAYER_X  = 300f;
    private static final float MONSTER_X = 1050f;
    private static final float CHAR_Y    = 280f;
    private static final float HP_BAR_WIDTH = CombatHealthBar.WIDTH;

    // Potion slots layout
    private static final float POTION_SLOT_SIZE = 44f;
    private static final float POTION_SLOT_X = 560f;
    private static final float POTION_BASE_Y = Constants.VIEWPORT_HEIGHT - 53f;

    public HUD(GameState initialState, ChangeListener endTurnListener, CombatUiAssets art) {
        this.snapshot = initialState;
        this.art = art;
        Pixmap headerPixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        headerPixel.setColor(0.1f, 0.1f, 0.15f, 0.85f);
        headerPixel.fill();
        barTexture = new Texture(headerPixel);
        headerPixel.dispose();

        font = UiTheme.font(16f);

        largeFont = UiTheme.font(30f);
        largeFont.setColor(Color.WHITE);

        smallFont = UiTheme.font(13f);

        tinyFont = UiTheme.font(13f);

        healthBar = new CombatHealthBar();
        tooltipStyle = new TextTooltip.TextTooltipStyle(new Label.LabelStyle(font, Color.WHITE), null);
        tooltipStyle.background = UiTheme.panel(new Color(0.04f, 0.05f, 0.07f, 0.98f),
                new Color(0.45f, 0.39f, 0.25f, 1f));
        tooltipStyle.wrapWidth = 260f;

        // Build potion slot buttons (3 slots)
        buildPotionSlots();

        // End Turn button
        TextButton.TextButtonStyle btnStyle = UiTheme.button(font);
        btnStyle.up = buttonFace(new Color(0.8f, 0.86f, 0.9f, 1f));
        btnStyle.over = buttonFace(Color.WHITE);
        btnStyle.focused = btnStyle.over;
        btnStyle.down = buttonFace(new Color(0.55f, 0.8f, 0.82f, 1f));
        btnStyle.disabled = buttonFace(Color.DARK_GRAY);
        endTurnBtn = new TextButton("END TURN", btnStyle);
        endTurnBtn.setSize(180, 60);
        endTurnBtn.setPosition(Constants.VIEWPORT_WIDTH - 210, 180);
        endTurnBtn.addListener(endTurnListener);
        addActor(endTurnBtn);

        // Draw pile click area
        Actor drawPileHit = new Actor();
        drawPileHit.setPosition(20, 15);
        drawPileHit.setSize(100, 90);
        drawPileHit.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (pileCallback != null) pileCallback.onDrawPileClicked();
            }
        });
        addActor(drawPileHit);

        // Discard pile click area
        Actor discardPileHit = new Actor();
        discardPileHit.setPosition(Constants.VIEWPORT_WIDTH - 120, 15);
        discardPileHit.setSize(100, 90);
        discardPileHit.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (pileCallback != null) pileCallback.onDiscardPileClicked();
            }
        });
        addActor(discardPileHit);
        addNavigation(art.deck, Constants.VIEWPORT_WIDTH - 208f, "View deck", () -> {
            if (navigationCallback != null) navigationCallback.onDeckClicked();
        });
        addNavigation(art.map, Constants.VIEWPORT_WIDTH - 108f, "View map", () -> {
            if (navigationCallback != null) navigationCallback.onMapClicked();
        });
        refreshPotionTextures();
    }

    private Drawable buttonFace(Color tint) {
        TextureRegionDrawable face = new TextureRegionDrawable(art.endTurn);
        face.setMinWidth(0f);
        face.setMinHeight(0f);
        face.setLeftWidth(20f);
        face.setRightWidth(20f);
        return face.tint(tint);
    }

    private void addNavigation(TextureRegion icon, float x, String label, Runnable onClick) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = new TextureRegionDrawable(icon);
        style.imageOver = new TextureRegionDrawable(icon).tint(new Color(0.7f, 1f, 1f, 1f));
        ImageButton button = new ImageButton(style);
        button.getImageCell().size(38f, 38f);
        button.setBounds(x, Constants.VIEWPORT_HEIGHT - 53f, 80f, 44f);
        button.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { onClick.run(); }
        });
        button.addListener(new TextTooltip(label, tooltipStyle));
        addActor(button);
    }

    public void setNavigationCallback(NavigationCallback callback) { navigationCallback = callback; }

    // ── Potion slots ──────────────────────────────────────────

    private void buildPotionSlots() {
        // We use invisible actors as hit-areas for the three potion slots
        for (int i = 0; i < 3; i++) {
            final int slotIndex = i;
            float sx = POTION_SLOT_X + i * 62f;
            float sy = POTION_BASE_Y;
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
                    if (potionCallback != null && slotIndex < RunManager.getInstance().getPotions().size()) {
                        potionCallback.onPotionClicked(slotIndex);
                    }
                }
            });
            TextTooltip tooltip = new TextTooltip("Empty potion slot", tooltipStyle);
            potionTooltips.add(tooltip);
            hitArea.addListener(tooltip);
            addActor(hitArea);
        }
    }

    /** Set the callback that fires when the player clicks a potion slot. */
    public void setPotionCallback(PotionClickCallback callback) {
        this.potionCallback = callback;
    }

    public void setPileCallback(PileClickCallback callback) {
        this.pileCallback = callback;
    }

    /** Reload the potion image cache from current RunManager state. */
    private void refreshPotionTextures() {
        // Dispose old
        for (Texture t : potionTextures) {
            if (t != null) t.dispose();
        }
        potionTextures.clear();

        List<AbstractPotion> potions = RunManager.getInstance().getPotions();
        for (AbstractPotion p : potions) {
            Texture tex = null;
            String imagePath = p.imagePath;
            if (imagePath == null || imagePath.isEmpty() || !Gdx.files.internal(imagePath).exists()) {
                imagePath = "IMAGES/PotionImages/" + switch (p.id) {
                    case "adrenaline_syringe" -> "BlockPotion.png";
                    case "vial_of_acid" -> "PoisonPotion.png";
                    case "steroid_ampoule" -> "StrengthPotion.png";
                    default -> "EnergyPotion.png";
                };
            }
            if (!imagePath.isEmpty()) {
                try {
                    if (Gdx.files.internal(imagePath).exists()) {
                        tex = new Texture(Gdx.files.internal(imagePath));
                        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    }
                } catch (Exception ignored) {}
            }
            potionTextures.add(tex);
        }
        for (int i = 0; i < potionTooltips.size(); i++) {
            potionTooltips.get(i).getActor().setText(i < potions.size()
                    ? potions.get(i).name + "\n" + potions.get(i).description : "Empty potion slot");
        }
    }

    // ── Update ────────────────────────────────────────────────

    public void update(GameState state) {
        this.snapshot = state;
        endTurnBtn.setDisabled(!state.isPlayerTurn());
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
        batch.draw(barTexture, 0f, h - 50f, w, 50f);

        // Top-left: Heart icon + HP
        float topY = h - 23f;
        CombatUiAssets.drawFitted(batch, art.heart, 24f, h - 48f, 32f, 36f);
        font.setColor(1f, 0.55f, 0.55f, 1f);
        font.draw(batch, snapshot.playerHp + "/" + snapshot.playerMaxHp, 66f, topY);

        // Top: Gold icon + count
        CombatUiAssets.drawFitted(batch, art.gold, 188f, h - 48f, 36f, 36f);
        font.setColor(Color.GOLD);
        font.draw(batch, "" + RunManager.getInstance().getGold(), 234f, topY);
        font.setColor(0.92f, 0.88f, 0.8f, parentAlpha);
        font.draw(batch, "FLOOR " + (RunManager.getInstance().getCurrentNodeIndex() + 1), 355f, topY);

        // ── Player HP bar ─────────────────────────────────────
        float playerBarY = CHAR_Y - CombatHealthBar.OFFSET_BELOW_FEET;
        healthBar.draw(batch, smallFont, PLAYER_X - HP_BAR_WIDTH / 2f, playerBarY,
                snapshot.playerHp, snapshot.playerMaxHp, snapshot.playerBlock, parentAlpha);

        // Player name
        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch,
            RunManager.getInstance().getSelectedCharacter() != null
                ? RunManager.getInstance().getSelectedCharacter().name() : "Player",
            PLAYER_X - HP_BAR_WIDTH / 2f, CHAR_Y + 300f, HP_BAR_WIDTH, Align.center, false);

        // Player status effects
        drawStatusEffects(batch, snapshot.playerStatus.summaryString(),
            PLAYER_X - HP_BAR_WIDTH / 2f, playerBarY - 14f);

        // ── Potion slots (bottom-left, above energy) ──────────
        drawPotionSlots(batch, parentAlpha);

        // ── Energy orb ───────────────────────────────────────
        float energyX = 30f;
        float energyY = 180f;
        float energySize = 112f;
        batch.setColor(1f, 1f, 1f, parentAlpha);
        CombatUiAssets.drawFitted(batch, art.energy, energyX, energyY, energySize, energySize);
        String energyStr = snapshot.playerEnergy + "/" + snapshot.playerMaxEnergy;
        float baseline = energyY + (energySize + largeFont.getCapHeight()) / 2f;
        largeFont.setColor(0.01f, 0.05f, 0.06f, parentAlpha);
        largeFont.draw(batch, energyStr, energyX + 2f, baseline - 2f, energySize, Align.center, false);
        largeFont.setColor(1f, 1f, 1f, parentAlpha);
        largeFont.draw(batch, energyStr, energyX, baseline, energySize, Align.center, false);

        // ── End Turn button background ────────────────────────

        // ── Draw pile (bottom-left corner) ───────────────────
        drawPile(batch, art.drawPile, 20f, snapshot.drawPile.size(), "DRAW", parentAlpha);

        // ── Discard pile (bottom-right corner) ───────────────
        drawPile(batch, art.discardPile, w - 110f, snapshot.discardPile.size(), "DISCARD", parentAlpha);

        batch.setColor(1, 1, 1, 1);
        super.draw(batch, parentAlpha);
    }

    /** Draws the three potion holsters within the header. */
    private void drawPotionSlots(Batch batch, float parentAlpha) {
        List<AbstractPotion> potions = RunManager.getInstance().getPotions();
        int maxSlots = 3;

        for (int i = 0; i < maxSlots; i++) {
            float sx = POTION_SLOT_X + i * 62f;
            float sy = POTION_BASE_Y;
            batch.setColor(1f, 1f, 1f, parentAlpha);
            CombatUiAssets.drawFitted(batch, art.potionSlot, sx, sy, POTION_SLOT_SIZE, POTION_SLOT_SIZE);

            if (i < potions.size()) {
                // Filled slot
                batch.setColor(1, 1, 1, parentAlpha);

                // Potion image or label
                if (i < potionTextures.size() && potionTextures.get(i) != null) {
                    CombatUiAssets.drawFitted(batch, new TextureRegion(potionTextures.get(i)),
                            sx + 8f, sy + 5f, POTION_SLOT_SIZE - 16f, POTION_SLOT_SIZE - 10f);
                }

                // Show name on hover / always
            }
        }
    }

    private void drawPile(Batch batch, TextureRegion icon, float x, int count, String label, float alpha) {
        batch.setColor(1f, 1f, 1f, alpha);
        CombatUiAssets.drawFitted(batch, icon, x, 30f, 80f, 66f);
        font.setColor(0.02f, 0.03f, 0.05f, alpha);
        font.draw(batch, Integer.toString(count), x + 60f, 46f, 30f, Align.center, false);
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(batch, Integer.toString(count), x + 59f, 47f, 30f, Align.center, false);
        tinyFont.setColor(0.86f, 0.83f, 0.74f, alpha);
        tinyFont.draw(batch, label, x, 23f, 90f, Align.center, false);
    }

    /** Draw a small coloured status summary string. */
    private void drawStatusEffects(Batch batch, String summary, float x, float y) {
        if (summary == null || summary.isEmpty()) return;
        tinyFont.setColor(new Color(0.9f, 0.7f, 0.2f, 1f));
        tinyFont.draw(batch, summary, x, y);
    }

    public float getPlayerX()  { return PLAYER_X;  }
    public float getMonsterX() { return MONSTER_X; }
    public float getCharY()    { return CHAR_Y;     }

    public void disposeResources() {
        font.dispose();
        largeFont.dispose();
        smallFont.dispose();
        tinyFont.dispose();
        healthBar.dispose();
        barTexture.dispose();
        for (Texture t : potionTextures) if (t != null) t.dispose();
        potionTextures.clear();
    }
}
