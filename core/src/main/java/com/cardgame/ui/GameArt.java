package com.cardgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;

import java.util.HashMap;
import java.util.Map;

/** Shared original artwork; small pixel icons retain their sharp edges. */
public final class GameArt {
    public static final String BATTLE = "IMAGES/Backgrounds/battle1.png";
    public static final String BOSS_BATTLE = "IMAGES/Backgrounds/operating-room.jpg";
    public static final String SHOP = "IMAGES/Backgrounds/pharmacy-counter.png";
    public static final String HEART = "IMAGES/play/heart.png";
    public static final String GOLD = "IMAGES/play/gold.png";
    public static final String REST = "IMAGES/play/restIcon.png";
    private static final Map<String, TextureRegion> ICONS = new HashMap<>();

    private GameArt() {}

    public static TextureRegion icon(String path) {
        return ICONS.computeIfAbsent(path, key -> {
            Pixmap source = new Pixmap(Gdx.files.internal(key));
            try {
                int left = source.getWidth(), top = source.getHeight(), right = -1, bottom = -1;
                for (int y = 0; y < source.getHeight(); y++) {
                    for (int x = 0; x < source.getWidth(); x++) {
                        if ((source.getPixel(x, y) & 0xff) > 8) {
                            left = Math.min(left, x); top = Math.min(top, y);
                            right = Math.max(right, x); bottom = Math.max(bottom, y);
                        }
                    }
                }
                Texture texture = new Texture(source);
                boolean pixelArt = source.getWidth() <= 32 && source.getHeight() <= 32;
                Texture.TextureFilter filter = pixelArt ? Texture.TextureFilter.Nearest : Texture.TextureFilter.Linear;
                texture.setFilter(filter, filter);
                return right < left ? new TextureRegion(texture)
                        : new TextureRegion(texture, left, top, right - left + 1, bottom - top + 1);
            } finally {
                source.dispose();
            }
        });
    }

    public static Image image(String path) {
        Image image = new Image(icon(path));
        image.setScaling(Scaling.fit);
        image.setTouchable(Touchable.disabled);
        return image;
    }

    /** Fill a viewport by cropping evenly, without stretching the supplied painting. */
    public static void cover(Batch batch, Texture texture, float x, float y, float width, float height) {
        float scale = Math.max(width / texture.getWidth(), height / texture.getHeight());
        float w = texture.getWidth() * scale, h = texture.getHeight() * scale;
        batch.draw(texture, x + (width - w) / 2f, y + (height - h) / 2f, w, h);
    }

    public static void dispose() {
        for (TextureRegion icon : ICONS.values()) icon.getTexture().dispose();
        ICONS.clear();
    }
}
