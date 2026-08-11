package com.cardgame.logic.monsters;
import com.cardgame.logic.GameState;
import com.cardgame.data.StatusEffect;

public class FrenziedPatient extends AbstractMonster {
    private boolean firstTurn = true;
    
    public FrenziedPatient(float x, float y) {
        super("Frenzied Patient", 50, "IMAGES/play/monster1.png");
        this.drawX = x; this.drawY = y;
    }
    
    @Override
    public void rollMove() {
        if (firstTurn) { intentType = "BUFF"; intentValue = 0; }
        else { intentType = "ATTACK"; intentValue = 6 + status.get(StatusEffect.STRENGTH); }
    }
    
    @Override
    public void takeTurn(GameState state) {
        if (firstTurn) {
            firstTurn = false;
            status.apply(StatusEffect.STRENGTH, 3);
        } else {
            int dmg = intentValue;
            if (status.has(StatusEffect.WEAK)) dmg = (int)(dmg * 0.75f);
            
            if (state.playerBlock >= dmg) { state.playerBlock -= dmg; }
            else { 
                dmg -= state.playerBlock;
                state.playerBlock = 0; 
                state.playerHp -= dmg; 
            }
            
            status.apply(StatusEffect.STRENGTH, 1);
        }
        rollMove();
    }
}
