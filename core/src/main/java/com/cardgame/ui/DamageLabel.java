package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

/**
 * A floating label that rises upward and fades out over ~1.2 seconds.
 * Used for damage numbers, block gained, status effects, etc.
 */
public class DamageLabel extends Actor {

    private final String text;
    private final BitmapFont font;
    private float alpha = 1f;

    public DamageLabel(String text, Color color) {
        this.text = text;
        this.font = new BitmapFont();
        this.font.getData().setScale(1.8f);
        this.font.setColor(color);

        // Self-animating: rise 80px over 1.2s and fade out, then remove self
        addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveBy(0f, 80f, 1.2f),
                Actions.fadeOut(1.2f)
            ),
            Actions.removeActor()
        ));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        font.setColor(font.getColor().r, font.getColor().g, font.getColor().b, getColor().a * parentAlpha);
        font.draw(batch, text, getX(), getY());
    }

    public void dispose() {
        font.dispose();
    }
}
