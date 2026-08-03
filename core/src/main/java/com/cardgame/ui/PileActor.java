package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.cardgame.utils.Constants;

public class PileActor extends Actor {
    private final Texture cardBg;
    private final Texture borderTex;
    private final BitmapFont font;
    
    private int count = 0;
    private final String title;

    public PileActor(String title) {
        this.title = title;
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(1.2f);
        
        setSize(Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
        
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.15f, 0.15f, 0.2f, 1f));
        pm.fill();
        this.cardBg = new Texture(pm);
        
        pm.setColor(new Color(0.4f, 0.4f, 0.5f, 1f));
        pm.fill();
        this.borderTex = new Texture(pm);
        
        pm.dispose();
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), w = getWidth(), h = getHeight();
        float alpha = parentAlpha * getColor().a;
        
        batch.setColor(1, 1, 1, alpha);
        
        // Draw multiple cards slightly offset to look like a deck
        int visualStack = Math.min(5, Math.max(1, count / 2));
        if (count == 0) visualStack = 1; // Empty slot
        
        for (int i = 0; i < visualStack; i++) {
            float ox = i * 2f;
            float oy = i * 2f;
            batch.draw(borderTex, x + ox - 2, y + oy - 2, w + 4, h + 4);
            batch.draw(cardBg, x + ox, y + oy, w, h);
        }
        
        // Draw title and count
        font.setColor(new Color(1f, 1f, 1f, alpha * (count > 0 ? 1f : 0.3f)));
        font.draw(batch, title, x + 5, y + h - 10);
        
        font.getData().setScale(1.5f);
        font.draw(batch, String.valueOf(count), x + w / 2f - 10, y + h / 2f + 10);
        font.getData().setScale(1.2f);
    }
    
    public void dispose() {
        cardBg.dispose();
        borderTex.dispose();
        font.dispose();
    }
}
