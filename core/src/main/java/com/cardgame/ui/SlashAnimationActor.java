package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

/** One-shot impact effect. The battle owns the shared texture, never this actor. */
public final class SlashAnimationActor extends Actor {
    public static final float FRAME_DURATION = 0.035f;
    private final Animation<TextureRegion> animation;
    private final boolean additive;
    private final Runnable onStart;
    private float stateTime;
    private boolean started;

    public SlashAnimationActor(Texture sheet, int columns, int rows, float centerX, float centerY,
                               float angle, float size, Color tint, boolean additive) {
        this(frames(sheet, columns, rows), centerX, centerY, angle, size, tint, additive, 0f, null);
    }

    public SlashAnimationActor(Animation<TextureRegion> animation, float centerX, float centerY,
                               float angle, float size, Color tint, boolean additive,
                               float delay, Runnable onStart) {
        if (animation.getKeyFrames().length == 0 || animation.getFrameDuration() <= 0f
                || size <= 0f || delay < 0f) throw new IllegalArgumentException("Invalid slash timing or size");
        this.animation = animation;
        this.additive = additive;
        this.onStart = onStart;
        stateTime = -delay;
        setSize(size, size);
        setOrigin(size / 2f, size / 2f);
        setPosition(centerX - size / 2f, centerY - size / 2f);
        setRotation(angle);
        setColor(tint);
        setTouchable(Touchable.disabled);
    }

    /** Split once per battle; trim symmetric black padding from non-square cells. */
    public static Animation<TextureRegion> frames(Texture sheet, int columns, int rows) {
        if (columns <= 0 || rows <= 0 || sheet.getWidth() % columns != 0 || sheet.getHeight() % rows != 0) {
            throw new IllegalArgumentException("Slash sheet must contain an equal-sized frame grid");
        }
        int width = sheet.getWidth() / columns;
        int height = sheet.getHeight() / rows;
        int side = Math.min(width, height);
        TextureRegion[] frames = new TextureRegion[columns * rows];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                frames[row * columns + column] = new TextureRegion(sheet,
                        column * width + (width - side) / 2, row * height + (height - side) / 2, side, side);
            }
        }
        return new Animation<>(FRAME_DURATION, frames);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        if (stateTime >= animation.getAnimationDuration()) {
            remove();
        } else if (!started && stateTime >= 0f) {
            started = true;
            if (onStart != null) onStart.run();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (stateTime < 0f || stateTime >= animation.getAnimationDuration()) return;
        float previousColor = batch.getPackedColor();
        int source = batch.getBlendSrcFunc(), destination = batch.getBlendDstFunc();
        int sourceAlpha = batch.getBlendSrcFuncAlpha(), destinationAlpha = batch.getBlendDstFuncAlpha();
        boolean blending = batch.isBlendingEnabled();
        try {
            if (additive) {
                batch.enableBlending();
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            }
            Color tint = getColor();
            batch.setColor(tint.r, tint.g, tint.b, tint.a * parentAlpha);
            batch.draw(animation.getKeyFrame(stateTime, false), getX(), getY(), getOriginX(), getOriginY(),
                    getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        } finally {
            if (additive) {
                batch.setBlendFunctionSeparate(source, destination, sourceAlpha, destinationAlpha);
                if (!blending) batch.disableBlending();
            }
            batch.setPackedColor(previousColor);
        }
    }
}
