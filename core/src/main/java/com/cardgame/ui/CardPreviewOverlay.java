package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cardgame.data.CardData;
import com.cardgame.utils.Constants;

/**
 * A centered overlay that shows a large card preview with description
 * when the player hovers over a card in their hand.
 *
 * CardActor.setPreviewOverlay() connects this overlay to card hover events.
 */
public class CardPreviewOverlay extends Group {

    private static final float PREVIEW_W = Constants.CARD_WIDTH * 2f;
    private static final float PREVIEW_H = Constants.CARD_HEIGHT * 2f;
    private static final float PREVIEW_X = (Constants.VIEWPORT_WIDTH  - PREVIEW_W) / 2f;
    private static final float PREVIEW_Y = (Constants.VIEWPORT_HEIGHT - PREVIEW_H) / 2f + 50f;

    private static final Color ATTACK_COLOR = new Color(0.85f, 0.25f, 0.20f, 0.9f);
    private static final Color SKILL_COLOR  = new Color(0.20f, 0.50f, 0.85f, 0.9f);
    private static final Color POWER_COLOR  = new Color(0.85f, 0.75f, 0.20f, 0.9f);

    private CardData hoveredCard = null;

    private final BitmapFont nameFont;
    private final BitmapFont descFont;
    private final BitmapFont costFont;
    private final Texture bgTex;
    private Texture cardImageCache = null;
    private String cachedImagePath = null;

    public CardPreviewOverlay() {
        nameFont = new BitmapFont();
        nameFont.getData().setScale(1.4f);
        nameFont.setColor(Color.WHITE);

        descFont = new BitmapFont();
        descFont.getData().setScale(1.0f);
        descFont.setColor(new Color(0.9f, 0.9f, 0.85f, 1f));

        costFont = new BitmapFont();
        costFont.getData().setScale(2.0f);
        costFont.setColor(Color.WHITE);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.05f, 0.05f, 0.10f, 0.93f));
        pm.fill();
        bgTex = new Texture(pm);
        pm.dispose();

        setVisible(false);
        // Overlay doesn't block input for other actors
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    /** Show a preview of the given card. */
    public void show(CardData card) {
        this.hoveredCard = card;
        setVisible(true);
    }

    /** Hide the preview. */
    public void hide() {
        this.hoveredCard = null;
        setVisible(false);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!isVisible() || hoveredCard == null) return;

        // Semi-dark backdrop panel
        batch.setColor(1, 1, 1, 0.92f * parentAlpha);
        batch.draw(bgTex, PREVIEW_X - 12, PREVIEW_Y - 60, PREVIEW_W + 24, PREVIEW_H + 80);

        // Type-coloured border
        Color typeColor = typeColor();
        batch.setColor(typeColor.r, typeColor.g, typeColor.b, 0.85f * parentAlpha);
        float bw = 4f;
        batch.draw(bgTex, PREVIEW_X - bw - 12, PREVIEW_Y - 60 - bw,
                PREVIEW_W + 24 + bw * 2, PREVIEW_H + 80 + bw * 2);

        // Redraw background on top of border
        batch.setColor(1, 1, 1, 0.92f * parentAlpha);
        batch.draw(bgTex, PREVIEW_X - 12, PREVIEW_Y - 60, PREVIEW_W + 24, PREVIEW_H + 80);

        // Card image (if available)
        Texture img = getCardTexture();
        if (img != null) {
            batch.setColor(1, 1, 1, parentAlpha);
            batch.draw(img, PREVIEW_X, PREVIEW_Y + 10, PREVIEW_W, PREVIEW_H - 80);
        } else {
            // Gradient fallback
            batch.setColor(typeColor.r, typeColor.g, typeColor.b, 0.3f * parentAlpha);
            batch.draw(bgTex, PREVIEW_X, PREVIEW_Y + 10, PREVIEW_W, PREVIEW_H - 80);
        }

        // Card name
        batch.setColor(1, 1, 1, parentAlpha);
        nameFont.setColor(Color.WHITE);
        String displayName = hoveredCard.name() + (hoveredCard.isUpgraded() ? " ✦" : "");
        nameFont.draw(batch, displayName, PREVIEW_X + 10, PREVIEW_Y + PREVIEW_H - 60);

        // Energy cost badge
        costFont.setColor(Color.YELLOW);
        costFont.draw(batch, String.valueOf(hoveredCard.energyCost()),
                PREVIEW_X + PREVIEW_W - 32, PREVIEW_Y + PREVIEW_H - 60);

        // Description (word-wrapped manually by inserting newlines)
        descFont.setColor(new Color(0.9f, 0.9f, 0.85f, parentAlpha));
        descFont.draw(batch, hoveredCard.description(),
                PREVIEW_X + 10, PREVIEW_Y - 8, PREVIEW_W - 20,
                com.badlogic.gdx.utils.Align.left, true);

        // Stats line
        String stats = "";
        if (hoveredCard.damage() > 0)  stats += "DMG: " + hoveredCard.damage() + "  ";
        if (hoveredCard.defence() > 0) stats += "BLK: " + hoveredCard.defence() + "  ";
        if (!stats.isEmpty()) {
            descFont.setColor(new Color(1f, 0.85f, 0.4f, parentAlpha));
            descFont.draw(batch, stats.trim(), PREVIEW_X + 10, PREVIEW_Y - 38);
        }

        batch.setColor(1, 1, 1, 1);
    }

    private Color typeColor() {
        if (hoveredCard == null) return ATTACK_COLOR;
        switch (hoveredCard.cardType()) {
            case SKILL:  return SKILL_COLOR;
            case POWER:  return POWER_COLOR;
            default:     return ATTACK_COLOR;
        }
    }

    private Texture getCardTexture() {
        if (hoveredCard == null) return null;
        String path = hoveredCard.image();
        if (path == null || path.isEmpty()) return null;
        if (!path.equals(cachedImagePath)) {
            // Load or grab from CardActor's shared cache
            cachedImagePath = path;
            cardImageCache = CardActor.getCachedTexture(path);
        }
        return cardImageCache;
    }

    public void disposeResources() {
        nameFont.dispose();
        descFont.dispose();
        costFont.dispose();
        bgTex.dispose();
    }
}
