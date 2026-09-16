package com.cardgame.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Map;

/** Per-enemy playback state; frame textures are shared and owned by the battle screen. */
public final class EnemyAnimation {
    public enum State { IDLE, ATTACK, HURT, BUFF }

    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> attack;
    private final Animation<TextureRegion> hurt;
    private final Animation<TextureRegion> buff;
    private final float scale;
    private final Map<TextureRegion, Float> frameScales;
    private State state = State.IDLE;
    private float time;

    public EnemyAnimation(Animation<TextureRegion> idle, Animation<TextureRegion> attack,
            Animation<TextureRegion> hurt, Animation<TextureRegion> buff, float scale) {
        this(idle, attack, hurt, buff, scale, Map.of());
    }

    public EnemyAnimation(Animation<TextureRegion> idle, Animation<TextureRegion> attack,
            Animation<TextureRegion> hurt, Animation<TextureRegion> buff, float scale,
            Map<TextureRegion, Float> frameScales) {
        this.idle = idle;
        this.attack = attack;
        this.hurt = hurt;
        this.buff = buff;
        this.scale = scale;
        this.frameScales = frameScales;
    }

    public void play(State state) {
        this.state = state;
        time = 0f;
    }

    public void update(float delta) {
        time += delta;
        Animation<TextureRegion> current = animation();
        if (state != State.IDLE && (current == null || current.isAnimationFinished(time))) {
            play(State.IDLE);
        }
    }

    private Animation<TextureRegion> animation() {
        return switch (state) {
            case IDLE -> idle;
            case ATTACK -> attack;
            case HURT -> hurt;
            case BUFF -> buff;
        };
    }

    public TextureRegion frame() {
        Animation<TextureRegion> current = animation();
        if (current == null) current = idle;
        return current == null ? null : current.getKeyFrame(time, state == State.IDLE);
    }

    public State state() { return state; }
    public float time() { return time; }
    public float scale() {
        TextureRegion frame = frame();
        return frame == null ? scale : frameScales.getOrDefault(frame, scale);
    }
}
