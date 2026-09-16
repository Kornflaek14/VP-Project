package com.cardgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

/** Battle-owned illustrated UI assets. Card art and frames use their existing resources. */
public final class CombatUiAssets implements Disposable {
    private static final String ROOT = "IMAGES/ui/combat/";
    private final List<Texture> textures = new ArrayList<>();

    public final TextureRegion energy;
    public final TextureRegion endTurn;
    public final TextureRegion heart;
    public final TextureRegion gold;
    public final TextureRegion potionSlot;
    public final TextureRegion deck;
    public final TextureRegion map;
    public final TextureRegion drawPile;
    public final TextureRegion discardPile;
    public final TextureRegion damageBadge;
    public final TextureRegion blockBadge;
    public final TextureRegion attackIntent;
    public final TextureRegion defendIntent;
    public final TextureRegion debuffIntent;
    public final TextureRegion buffIntent;

    public CombatUiAssets() {
        try {
            energy = load("energy-orb", 1, 256)[0];
            endTurn = load("end-turn", 1, 560)[0];
            heart = load("heart", 1, 128)[0];
            gold = load("gold", 1, 128)[0];
            potionSlot = load("potion-slot", 1, 128)[0];
            TextureRegion[] navigation = load("navigation", 2, 128);
            deck = navigation[0];
            map = navigation[1];
            TextureRegion[] piles = load("piles", 2, 128);
            drawPile = piles[0];
            discardPile = piles[1];
            TextureRegion[] badges = load("badges", 2, 128);
            damageBadge = badges[0];
            blockBadge = badges[1];
            TextureRegion[] intents = load("intents", 4, 128);
            attackIntent = intents[0];
            defendIntent = intents[1];
            debuffIntent = intents[2];
            buffIntent = intents[3];
        } catch (RuntimeException e) {
            dispose();
            throw e;
        }
    }

    /** Trim transparent padding per sheet cell and upload only a UI-sized texture. */
    private TextureRegion[] load(String name, int columns, int maxSide) {
        Pixmap source = new Pixmap(Gdx.files.internal(ROOT + name + ".png"));
        try {
            TextureRegion[] regions = new TextureRegion[columns];
            for (int column = 0; column < columns; column++) {
                int left = source.getWidth() * column / columns;
                int right = source.getWidth() * (column + 1) / columns;
                int minX = right, minY = source.getHeight(), maxX = left - 1, maxY = -1;
                for (int y = 0; y < source.getHeight(); y++) {
                    for (int x = left; x < right; x++) {
                        if ((source.getPixel(x, y) & 0xff) > 8) {
                            minX = Math.min(minX, x);
                            minY = Math.min(minY, y);
                            maxX = Math.max(maxX, x);
                            maxY = Math.max(maxY, y);
                        }
                    }
                }
                if (maxX < minX) throw new IllegalArgumentException("Empty UI sprite: " + name + " cell " + column);
                int width = maxX - minX + 1, height = maxY - minY + 1;
                float scale = Math.min(1f, (float) maxSide / Math.max(width, height));
                Pixmap resized = new Pixmap(Math.max(1, Math.round(width * scale)),
                        Math.max(1, Math.round(height * scale)), Pixmap.Format.RGBA8888);
                try {
                    resized.setBlending(Pixmap.Blending.None);
                    resized.setFilter(Pixmap.Filter.BiLinear);
                    resized.drawPixmap(source, minX, minY, width, height, 0, 0, resized.getWidth(), resized.getHeight());
                    Texture texture = new Texture(resized, true);
                    texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
                    textures.add(texture);
                    regions[column] = new TextureRegion(texture);
                } finally {
                    resized.dispose();
                }
            }
            return regions;
        } finally {
            source.dispose();
        }
    }

    public static void drawFitted(Batch batch, TextureRegion image, float x, float y, float width, float height) {
        float scale = Math.min(width / image.getRegionWidth(), height / image.getRegionHeight());
        float w = image.getRegionWidth() * scale, h = image.getRegionHeight() * scale;
        batch.draw(image, x + (width - w) / 2f, y + (height - h) / 2f, w, h);
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) texture.dispose();
        textures.clear();
    }
}
