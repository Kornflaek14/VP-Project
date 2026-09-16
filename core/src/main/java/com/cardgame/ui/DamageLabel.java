package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

/**
 * Combat badge that pops, bounces down, then drifts upward and dissolves.
 * Icons and the font atlas are shared resources owned outside this actor.
 */
public class DamageLabel extends Group {

    public static final float DURATION = 0.12f + 0.45f + 0.35f;
    private static final float ICON_SIZE = 28f;
    private static final float ICON_GAP = 6f;

    private final String text;
    private final BitmapFont font;
    private final TextureRegion icon;
    private final float textHeight;

    public DamageLabel(String text, Color color) {
        this(text, color, (TextureRegion) null, false);
    }

    public DamageLabel(String text, Color color, Texture icon, boolean isHeavyHit) {
        this(text, color, icon == null ? null : new TextureRegion(icon), isHeavyHit);
    }

    public DamageLabel(String text, Color color, TextureRegion icon, boolean isHeavyHit) {
        this.text = text;
        this.icon = icon;
        this.font = UiTheme.font(26f);
        GlyphLayout layout = new GlyphLayout(font, text);
        textHeight = layout.height;
        setColor(color);
        setTouchable(Touchable.disabled);
        setSize(layout.width + (icon == null ? 0f : ICON_SIZE + ICON_GAP),
                Math.max(icon == null ? 0f : ICON_SIZE, textHeight));
        setOrigin(getWidth() / 2f, getHeight() / 2f);
        float baseScale = isHeavyHit ? 1.35f : 1f;
        setScale(baseScale * 1.7f);
        float driftX = MathUtils.random(-25f, 25f);

        addAction(Actions.sequence(
            Actions.scaleTo(baseScale, baseScale, 0.12f, Interpolation.swingOut),
            Actions.moveBy(driftX, 35f, 0.45f, Interpolation.pow2Out),
            Actions.parallel(
                Actions.moveBy(driftX * 0.5f, 40f, 0.35f),
                Actions.fadeOut(0.35f)
            ),
            Actions.removeActor()
        ));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color tint = getColor();
        float alpha = tint.a * parentAlpha;
        if (alpha <= 0f) return;

        float previousColor = batch.getPackedColor();
        // Transform the badge and text together about their shared center, preserving
        // UiTheme's font metrics instead of replacing its atlas scale with actor scale.
        applyTransform(batch, computeTransform());
        try {
            float textX = 0f;
            if (icon != null) {
                batch.setColor(1f, 1f, 1f, alpha);
                CombatUiAssets.drawFitted(batch, icon, 0f, (getHeight() - ICON_SIZE) / 2f, ICON_SIZE, ICON_SIZE);
                textX = ICON_SIZE + ICON_GAP;
            }
            float textY = (getHeight() + textHeight) / 2f;
            font.setColor(0f, 0f, 0f, alpha * 0.75f);
            font.draw(batch, text, textX + 2f, textY - 2f);
            font.setColor(tint.r, tint.g, tint.b, alpha);
            font.draw(batch, text, textX, textY);
        } finally {
            resetTransform(batch);
            batch.setPackedColor(previousColor);
        }
    }

    public void dispose() {
        font.dispose();
    }
}
