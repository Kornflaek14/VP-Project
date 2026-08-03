package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.cardgame.data.CardData;
import com.cardgame.logic.CardInstance;
import com.cardgame.utils.Constants;

public class CardDetailOverlay extends Actor {
    private CardInstance card;
    private final BitmapFont fontLarge;
    private final BitmapFont fontSmall;
    private final Texture bgTex;

    public CardDetailOverlay() {
        fontLarge = new BitmapFont();
        fontLarge.getData().setScale(1.5f);
        fontSmall = new BitmapFont();
        
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.1f, 0.1f, 0.15f, 0.95f));
        pm.fill();
        bgTex = new Texture(pm);
        pm.dispose();
        
        float w = Constants.CARD_WIDTH * 2f;
        float h = Constants.CARD_HEIGHT * 2f;
        setSize(w, h);
        setPosition((Constants.VIEWPORT_WIDTH - w) / 2f, (Constants.VIEWPORT_HEIGHT - h) / 2f);
    }

    public void setCard(CardInstance card) {
        this.card = card;
        setVisible(card != null);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (card == null || !isVisible()) return;

        float x = getX(), y = getY(), w = getWidth(), h = getHeight();
        
        batch.setColor(1, 1, 1, parentAlpha);
        batch.draw(bgTex, x, y, w, h);

        CardData template = card.getTemplate();
        
        fontLarge.setColor(Color.WHITE);
        fontLarge.draw(batch, template.name(), x + 15, y + h - 20);
        
        fontSmall.setColor(Color.CYAN);
        fontSmall.draw(batch, "Blood: " + template.bloodCost() + " | Bones: " + template.boneCost(), x + 15, y + h - 60);

        fontSmall.setColor(Color.GOLD);
        fontSmall.draw(batch, "Type: " + template.cardType() + " | Aff: " + template.affinityType(), x + 15, y + h - 90);

        fontSmall.setColor(Color.WHITE);
        String desc = template.description();
        if (desc == null || desc.isBlank()) {
            desc = String.join(", ", template.abilityIds());
        }
        fontSmall.draw(batch, desc, x + 15, y + h - 140, w - 30, com.badlogic.gdx.utils.Align.left, true);
        
        fontLarge.setColor(new Color(0.95f, 0.35f, 0.25f, 1f));
        fontLarge.draw(batch, "ATK: " + template.attack(), x + 15, y + 40);
        
        fontLarge.setColor(new Color(0.25f, 0.80f, 0.35f, 1f));
        fontLarge.draw(batch, "HP: " + card.getCurrentHealth() + "/" + template.health(), x + w / 2, y + 40);
    }

    public void dispose() {
        fontLarge.dispose();
        fontSmall.dispose();
        bgTex.dispose();
    }
}
