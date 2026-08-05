package com.cardgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.cardgame.data.CardData;

import java.util.HashMap;
import java.util.Map;

/**
 * Scene2D Actor for rendering a single card in STS style.
 * Displays: full card art image, energy cost badge, damage/block text, name, type color border.
 */
public class CardActor extends Actor {

    public interface OnClickCallback { void onClick(CardActor actor); }

    private final CardData card;
    private boolean hovered = false;

    // Textures
    private Texture cardImage;
    private Texture borderTex;
    private Texture costBg;
    private Texture statBg;

    private final BitmapFont font;
    private final BitmapFont smallFont;

    // Color coding by card type
    private static final Color ATTACK_COLOR = new Color(0.85f, 0.25f, 0.20f, 1f);
    private static final Color SKILL_COLOR  = new Color(0.20f, 0.50f, 0.85f, 1f);
    private static final Color POWER_COLOR  = new Color(0.85f, 0.75f, 0.20f, 1f);

    private static final Map<String, Texture> imageCache = new HashMap<>();

    public CardActor(CardData card, OnClickCallback callback) {
        this.card = card;
        this.font = new BitmapFont();
        this.font.getData().setScale(0.9f);
        this.smallFont = new BitmapFont();
        this.smallFont.getData().setScale(0.7f);

        buildTextures();

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (callback != null) callback.onClick(CardActor.this);
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hovered = true;
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hovered = false;
            }
        });
    }

    private void buildTextures() {
        Color typeColor;
        switch (card.cardType()) {
            case SKILL:  typeColor = SKILL_COLOR;  break;
            case POWER:  typeColor = POWER_COLOR;  break;
            default:     typeColor = ATTACK_COLOR; break;
        }
        borderTex = singlePixel(typeColor);
        costBg    = singlePixel(new Color(0.1f, 0.1f, 0.1f, 0.85f));
        statBg    = singlePixel(new Color(0.05f, 0.05f, 0.08f, 0.75f));

        // Load card image from assets
        String imagePath = card.image();
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imageCache.containsKey(imagePath)) {
                cardImage = imageCache.get(imagePath);
            } else {
                try {
                    if (Gdx.files.internal(imagePath).exists()) {
                        cardImage = new Texture(Gdx.files.internal(imagePath));
                        cardImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                        imageCache.put(imagePath, cardImage);
                    }
                } catch (Exception e) {
                    cardImage = null;
                }
            }
        }
    }

    private static Texture singlePixel(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    public CardData getCard() { return card; }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float w = getWidth();
        float h = getHeight();
        float x = getX();
        float y = getY();
        float alpha = parentAlpha * getColor().a;

        // Lift on hover
        float liftY = hovered ? 20f : 0f;
        float scale = hovered ? 1.08f : 1f;
        float sw = w * scale;
        float sh = h * scale;
        float sx = x - (sw - w) / 2f;
        float sy = y + liftY - (sh - h) / 2f;

        // Border (3px)
        float bw = 3f;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(borderTex, sx - bw, sy - bw, sw + bw * 2, sh + bw * 2);

        // Card image or fallback
        if (cardImage != null) {
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(cardImage, sx, sy, sw, sh);
        } else {
            batch.setColor(0.15f, 0.15f, 0.25f, alpha);
            batch.draw(costBg, sx, sy, sw, sh);
        }

        // Dark overlay at bottom for text
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(statBg, sx, sy, sw, sh * 0.28f);

        // Energy cost badge (top-left circle)
        float badgeR = 18f * scale;
        batch.draw(costBg, sx + 4f, sy + sh - badgeR * 2 - 4f, badgeR * 2, badgeR * 2);
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(batch, String.valueOf(card.energyCost()),
                sx + 4f + badgeR * 0.55f, sy + sh - 4f - badgeR * 0.55f);

        // Card name (bottom area)
        smallFont.setColor(1f, 1f, 1f, alpha);
        String name = card.name();
        if (name.length() > 14) name = name.substring(0, 13) + "…";
        smallFont.draw(batch, name, sx + 6f, sy + sh * 0.24f);

        // Damage (bottom-left)
        if (card.damage() > 0) {
            font.setColor(1f, 0.35f, 0.25f, alpha);
            font.draw(batch, "⚔" + card.damage(), sx + 6f, sy + sh * 0.12f);
        }

        // Block (bottom-right)
        if (card.defence() > 0) {
            font.setColor(0.3f, 0.7f, 1f, alpha);
            font.draw(batch, "🛡" + card.defence(), sx + sw - 45f * scale, sy + sh * 0.12f);
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void dispose() {
        borderTex.dispose();
        costBg.dispose();
        statBg.dispose();
        font.dispose();
        smallFont.dispose();
        // Don't dispose cardImage — it's cached
    }
}
