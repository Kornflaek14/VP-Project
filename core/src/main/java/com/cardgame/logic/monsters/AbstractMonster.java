package com.cardgame.logic.monsters;

import com.cardgame.logic.GameState;
import com.cardgame.logic.StatusEffectState;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

public abstract class AbstractMonster {
    public String id, name;
    public int maxHp, currentHp, block;
    public String imagePath;
    
    public String intentType = "ATTACK";
    public int intentValue = 0;
    
    public StatusEffectState status = new StatusEffectState();
    
    public float drawX, drawY;
    private Texture texture;
    
    public AbstractMonster(String name, int maxHp, String imagePath) {
        this.name = name;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.imagePath = imagePath;
    }
    
    public abstract void takeTurn(GameState state);
    public abstract void rollMove();
    
    public void damage(int amount) {
        if (block > 0) {
            if (amount <= block) {
                block -= amount;
                return;
            } else {
                amount -= block;
                block = 0;
            }
        }
        currentHp -= amount;
    }
    
    public Texture getTexture() {
        if (texture == null) {
            try {
                texture = new Texture(Gdx.files.internal(imagePath));
            } catch (Exception e) {
                texture = new Texture(Gdx.files.internal("IMAGES/play/monster.png"));
            }
        }
        return texture;
    }
    
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
