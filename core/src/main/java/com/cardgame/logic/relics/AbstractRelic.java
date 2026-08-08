package com.cardgame.logic.relics;

import com.cardgame.logic.GameState;
import com.cardgame.logic.cards.AbstractCard;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

public abstract class AbstractRelic {
    public String id, name, description, imagePath;
    private Texture texture;
    public int counter = -1; // -1 means no counter
    
    public AbstractRelic(String id, String name, String description, String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
    }
    
    // Hooks
    public void onEquip() {}
    public void atBattleStart(GameState state) {}
    public void atTurnStart(GameState state) {}
    public void onPlayCard(AbstractCard card, GameState state) {}
    public void onVictory() {}
    
    public Texture getTexture() {
        if (texture == null) {
            try {
                texture = new Texture(Gdx.files.internal(imagePath));
            } catch (Exception e) {
                texture = new Texture(Gdx.files.internal("IMAGES/play/relic.png")); // fallback
            }
        }
        return texture;
    }
    
    public void dispose() {
        if (texture != null) texture.dispose();
    }
    
    public abstract AbstractRelic makeCopy();
}
