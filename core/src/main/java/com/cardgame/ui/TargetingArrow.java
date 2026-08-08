package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class TargetingArrow extends Actor {

    private final Texture dotTexture;
    private final Texture headTexture;
    public final Vector2 start = new Vector2();
    public final Vector2 end = new Vector2();
    
    public TargetingArrow() {
        dotTexture = createCircle(6, Color.WHITE);
        headTexture = createCircle(14, Color.RED);
        setVisible(false);
    }

    private Texture createCircle(int radius, Color c) {
        int size = radius * 2;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fillCircle(radius, radius, radius);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!isVisible()) return;

        // Create a nice arc
        float dy = end.y - start.y;
        float dx = end.x - start.x;
        
        Vector2 p1 = new Vector2(start.x, start.y + 200f);
        Vector2 p2 = new Vector2(end.x, end.y - 150f);

        Bezier<Vector2> bezier = new Bezier<>(start, p1, p2, end);

        int segments = 20;
        Vector2 tmp = new Vector2();
        batch.setColor(1, 1, 1, parentAlpha * 0.8f);
        for (int i = 1; i < segments; i++) {
            float t = (float) i / (segments);
            bezier.valueAt(tmp, t);
            batch.draw(dotTexture, tmp.x - dotTexture.getWidth()/2f, tmp.y - dotTexture.getHeight()/2f);
        }

        // Draw reticle head at the exact end position
        batch.setColor(1, 0, 0, parentAlpha);
        batch.draw(headTexture, end.x - headTexture.getWidth()/2f, end.y - headTexture.getHeight()/2f);
        batch.setColor(1, 1, 1, 1);
    }

    public void dispose() {
        dotTexture.dispose();
        headTexture.dispose();
    }
}
