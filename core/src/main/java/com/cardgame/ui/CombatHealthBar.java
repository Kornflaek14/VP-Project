package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

/** Shared player/enemy health bar with a shield badge overlapping its left edge. */
public final class CombatHealthBar implements Disposable {
    public static final float WIDTH = 180f;
    public static final float HEIGHT = 14f;
    public static final float OFFSET_BELOW_FEET = 25f;

    private final Texture roundedTexture;
    private final NinePatch rounded;
    private final Texture shieldTexture;

    public CombatHealthBar() {
        Pixmap shape = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        shape.setColor(Color.WHITE);
        shape.fillRectangle(4, 0, 8, 16);
        shape.fillRectangle(0, 4, 16, 8);
        shape.fillCircle(4, 4, 4);
        shape.fillCircle(11, 4, 4);
        shape.fillCircle(4, 11, 4);
        shape.fillCircle(11, 11, 4);
        roundedTexture = new Texture(shape);
        roundedTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        rounded = new NinePatch(roundedTexture, 4, 4, 4, 4);
        shape.dispose();

        Pixmap shield = new Pixmap(48, 56, Pixmap.Format.RGBA8888);
        shield.setColor(0.04f, 0.12f, 0.17f, 1f);
        fillShield(shield, new int[] {24, 0, 47, 10, 42, 37, 24, 55, 6, 37, 0, 10});
        shield.setColor(0.6f, 0.96f, 1f, 1f);
        fillShield(shield, new int[] {24, 3, 44, 12, 39, 36, 24, 51, 9, 36, 3, 12});
        shield.setColor(0.14f, 0.42f, 0.54f, 1f);
        fillShield(shield, new int[] {24, 7, 40, 14, 35, 34, 24, 46, 13, 34, 7, 14});
        shield.setColor(0.22f, 0.55f, 0.66f, 1f);
        shield.fillTriangle(24, 7, 7, 14, 24, 46);
        shieldTexture = new Texture(shield);
        shieldTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        shield.dispose();
    }

    private static void fillShield(Pixmap pixmap, int[] vertices) {
        for (int i = 0; i < vertices.length; i += 2) {
            int next = (i + 2) % vertices.length;
            pixmap.fillTriangle(24, 27, vertices[i], vertices[i + 1], vertices[next], vertices[next + 1]);
        }
    }

    public void draw(Batch batch, BitmapFont font, float x, float y,
                     int hp, int maxHp, int block, float alpha) {
        float previousBatchColor = batch.getPackedColor();
        Color fontColor = font.getColor();
        float oldR = fontColor.r, oldG = fontColor.g, oldB = fontColor.b, oldA = fontColor.a;
        boolean shielded = block > 0;
        try {
            batch.setColor(0.025f, 0.035f, 0.045f, alpha * 0.9f);
            rounded.draw(batch, x - 2f, y - 2f, WIDTH + 4f, HEIGHT + 4f);
            // The cyan border spans the whole health capacity while block is present.
            if (shielded) batch.setColor(0.58f, 0.94f, 1f, alpha);
            else batch.setColor(0.42f, 0.13f, 0.15f, alpha);
            rounded.draw(batch, x, y, WIDTH, HEIGHT);
            batch.setColor(0.045f, 0.055f, 0.075f, alpha);
            rounded.draw(batch, x + 2f, y + 2f, WIDTH - 4f, HEIGHT - 4f);

            float ratio = maxHp > 0 ? MathUtils.clamp((float) hp / maxHp, 0f, 1f) : 0f;
            float fillWidth = (WIDTH - 4f) * ratio;
            if (fillWidth > 0f) {
                if (shielded) batch.setColor(0.22f, 0.52f, 0.9f, alpha);
                else batch.setColor(0.82f, 0.16f, 0.19f, alpha);
                if (fillWidth >= 8f) rounded.draw(batch, x + 2f, y + 2f, fillWidth, HEIGHT - 4f);
                else batch.draw(roundedTexture, x + 2f, y + 2f, fillWidth, HEIGHT - 4f);
            }

            drawNumber(batch, font, Math.max(0, hp) + "/" + Math.max(0, maxHp),
                    x, y + (HEIGHT + font.getCapHeight()) / 2f, WIDTH, alpha);
            batch.setColor(1f, 1f, 1f, alpha);
            CombatUiAssets.drawFitted(batch, GameArt.icon(GameArt.HEART), x + WIDTH + 7f, y - 3f, 21f, 21f);
            if (shielded) {
                float badgeWidth = 32f, badgeHeight = 36f;
                float badgeX = x - badgeWidth / 2f + 2f;
                float badgeY = y + (HEIGHT - badgeHeight) / 2f;
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(shieldTexture, badgeX, badgeY, badgeWidth, badgeHeight);
                drawNumber(batch, font, Integer.toString(block), badgeX,
                        badgeY + (badgeHeight + font.getCapHeight()) / 2f + 1f, badgeWidth, alpha);
            }
        } finally {
            font.setColor(oldR, oldG, oldB, oldA);
            batch.setPackedColor(previousBatchColor);
        }
    }

    private static void drawNumber(Batch batch, BitmapFont font, String text,
                                   float x, float y, float width, float alpha) {
        font.setColor(0.025f, 0.035f, 0.055f, alpha);
        font.draw(batch, text, x + 1f, y - 1f, width, Align.center, false);
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(batch, text, x, y, width, Align.center, false);
    }

    @Override
    public void dispose() {
        roundedTexture.dispose();
        shieldTexture.dispose();
    }
}
