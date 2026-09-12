package com.cardgame.logic.potions;

import com.cardgame.logic.GameState;
import com.cardgame.logic.monsters.AbstractMonster;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

public abstract class AbstractPotion {
    public String id, name, description, imagePath;
    private Texture texture;
    
    public AbstractPotion(String id, String name, String description, String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
    }
    
    public abstract boolean isTargeted();
    public abstract void use(GameState state, AbstractMonster target);
    
    public Texture getTexture() {
        if (texture == null) {
            try {
                texture = new Texture(Gdx.files.internal(imagePath));
            } catch (Exception e) {
                texture = new Texture(Gdx.files.internal("IMAGES/play/potion.png")); // fallback
            }
        }
        return texture;
    }
    
    public void dispose() {
        if (texture != null) texture.dispose();
    }
    
    public abstract AbstractPotion makeCopy();
}
