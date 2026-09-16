package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlashAnimationActorTest {
    private SlashAnimationActor slash(float delay, Runnable callback) {
        Animation<TextureRegion> animation = new Animation<>(0.035f, new TextureRegion(), new TextureRegion(),
                new TextureRegion(), new TextureRegion(), new TextureRegion(), new TextureRegion());
        return new SlashAnimationActor(animation, 800, 400, -35, 240, Color.CYAN, true, delay, callback);
    }

    @Test
    void delayedSlashStartsOnceAndRemovesItselfWithoutDisposingSharedFrames() {
        AtomicInteger starts = new AtomicInteger();
        SlashAnimationActor actor = slash(0.1f, starts::incrementAndGet);
        Group group = new Group();
        group.addActor(actor);
        Probe probe = new Probe();
        actor.act(0.09f);
        actor.draw(probe.batch, 1);
        assertEquals(0, starts.get());
        assertEquals(0, probe.draws);
        actor.act(0.02f);
        actor.draw(probe.batch, 1);
        assertEquals(1, starts.get());
        assertEquals(1, probe.draws);
        actor.act(0.19f);
        assertEquals(group, actor.getParent());
        actor.act(0.02f);
        assertNull(actor.getParent());
        assertEquals(1, starts.get());
    }

    @Test
    void additiveDrawRestoresColorSeparateAlphaAndDisabledBlending() {
        Probe probe = new Probe();
        float original = probe.packed;
        slash(0, null).draw(probe.batch, 0.5f);
        assertEquals(1, probe.draws);
        assertEquals(GL20.GL_SRC_ALPHA, probe.drawSource);
        assertEquals(GL20.GL_ONE, probe.drawDestination);
        assertEquals(0.5f, probe.drawAlpha);
        assertEquals(original, probe.packed);
        assertEquals(GL20.GL_ONE, probe.source);
        assertEquals(GL20.GL_ZERO, probe.destination);
        assertEquals(GL20.GL_ONE_MINUS_DST_ALPHA, probe.sourceAlpha);
        assertEquals(GL20.GL_DST_ALPHA, probe.destinationAlpha);
        assertFalse(probe.blending);
    }

    @Test
    void renderFailureStillRestoresBatchState() {
        Probe probe = new Probe();
        probe.failDraw = true;
        float original = probe.packed;
        assertThrows(IllegalStateException.class, () -> slash(0, null).draw(probe.batch, 1));
        assertEquals(original, probe.packed);
        assertEquals(GL20.GL_ZERO, probe.destination);
        assertFalse(probe.blending);
    }

    private static final class Probe {
        float packed = Color.PINK.toFloatBits(), drawAlpha;
        int source = GL20.GL_ONE, destination = GL20.GL_ZERO;
        int sourceAlpha = GL20.GL_ONE_MINUS_DST_ALPHA, destinationAlpha = GL20.GL_DST_ALPHA;
        int draws, drawSource, drawDestination;
        boolean blending, failDraw;
        final Batch batch = (Batch) Proxy.newProxyInstance(Batch.class.getClassLoader(), new Class<?>[]{Batch.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getPackedColor": return packed;
                        case "getBlendSrcFunc": return source;
                        case "getBlendDstFunc": return destination;
                        case "getBlendSrcFuncAlpha": return sourceAlpha;
                        case "getBlendDstFuncAlpha": return destinationAlpha;
                        case "isBlendingEnabled": return blending;
                        case "enableBlending": blending = true; break;
                        case "disableBlending": blending = false; break;
                        case "setPackedColor": packed = (float) args[0]; break;
                        case "setColor": drawAlpha = (float) args[3]; break;
                        case "setBlendFunction":
                            source = sourceAlpha = (int) args[0];
                            destination = destinationAlpha = (int) args[1];
                            break;
                        case "setBlendFunctionSeparate":
                            source = (int) args[0]; destination = (int) args[1];
                            sourceAlpha = (int) args[2]; destinationAlpha = (int) args[3];
                            break;
                        case "draw":
                            assertTrue(blending);
                            draws++;
                            drawSource = source; drawDestination = destination;
                            if (failDraw) throw new IllegalStateException("Draw failed");
                            break;
                        default: throw new AssertionError("Unexpected batch call: " + method.getName());
                    }
                    return null;
                });
    }
}
