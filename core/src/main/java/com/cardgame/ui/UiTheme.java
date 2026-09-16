package com.cardgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/** Shared typography and controls. GPU resources live until the game exits. */
public final class UiTheme {
    private static Texture fontAtlas;
    private static Texture pixel;

    private UiTheme() {}

    /** Independent font metrics/colors over a shared, high-resolution atlas. */
    public static BitmapFont font(float capHeight) {
        if (fontAtlas == null) {
            fontAtlas = new Texture(Gdx.files.internal("fonts/locura-ui.png"), true);
            fontAtlas.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        }
        BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData(Gdx.files.internal("fonts/locura-ui.fnt"), false);
        BitmapFont font = new BitmapFont(data, new TextureRegion(fontAtlas), false);
        font.setOwnsTexture(false);
        font.setUseIntegerPositions(false);
        data.setScale(capHeight / data.capHeight);
        font.setColor(0.94f, 0.92f, 0.88f, 1f);
        return font;
    }

    public static TextButton.TextButtonStyle button(BitmapFont font) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = new Color(0.94f, 0.92f, 0.88f, 1f);
        style.overFontColor = Color.WHITE;
        style.downFontColor = Color.WHITE;
        style.focusedFontColor = Color.WHITE;
        style.disabledFontColor = new Color(0.57f, 0.57f, 0.60f, 1f);
        style.up = panel(new Color(0.075f, 0.08f, 0.105f, 0.96f), new Color(0.34f, 0.29f, 0.28f, 1f));
        style.over = panel(new Color(0.20f, 0.10f, 0.13f, 1f), new Color(0.75f, 0.47f, 0.38f, 1f));
        style.focused = style.over;
        style.down = panel(new Color(0.30f, 0.11f, 0.14f, 1f), new Color(0.94f, 0.69f, 0.48f, 1f));
        style.disabled = panel(new Color(0.065f, 0.065f, 0.08f, 0.96f), new Color(0.19f, 0.19f, 0.22f, 1f));
        return style;
    }

    public static Drawable panel(Color fill, Color border) {
        if (pixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            pixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return new BaseDrawable() {
            {
                setLeftWidth(16f);
                setRightWidth(16f);
                setTopHeight(10f);
                setBottomHeight(10f);
            }

            @Override
            public void draw(Batch batch, float x, float y, float width, float height) {
                float packed = batch.getPackedColor();
                float alpha = batch.getColor().a;
                batch.setColor(border.r, border.g, border.b, border.a * alpha);
                batch.draw(pixel, x, y, width, height);
                batch.setColor(fill.r, fill.g, fill.b, fill.a * alpha);
                batch.draw(pixel, x + 1f, y + 1f, Math.max(0f, width - 2f), Math.max(0f, height - 2f));
                batch.setPackedColor(packed);
            }
        };
    }

    public static void dispose() {
        if (fontAtlas != null) { fontAtlas.dispose(); fontAtlas = null; }
        if (pixel != null) { pixel.dispose(); pixel = null; }
    }
}
