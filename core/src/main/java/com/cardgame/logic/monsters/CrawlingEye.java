package com.cardgame.logic.monsters;
import com.cardgame.logic.GameState;
import com.cardgame.data.StatusEffect;
import java.util.concurrent.ThreadLocalRandom;

public class CrawlingEye extends AbstractMonster {
    public CrawlingEye(float x, float y) {
        super("Crawling Eye", 15, "IMAGES/play/monster.png");
        this.drawX = x; this.drawY = y;
        this.block = 5; // Start with Curl Up effectively
    }
    
    @Override
    public void rollMove() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 75) { intentType = "ATTACK"; intentValue = ThreadLocalRandom.current().nextInt(5, 8); }
        else { intentType = "BUFF"; intentValue = 0; }
    }
    
    @Override
    public void takeTurn(GameState state) {
        if (intentType.equals("ATTACK")) {
            int dmg = intentValue + status.get(StatusEffect.STRENGTH);
            if (status.has(StatusEffect.WEAK)) dmg = (int)(dmg * 0.75f);
            if (state.playerBlock >= dmg) { state.playerBlock -= dmg; }
            else { dmg -= state.playerBlock; state.playerBlock = 0; state.playerHp -= dmg; }
        } else {
            status.apply(StatusEffect.STRENGTH, 3);
        }
        rollMove();
    }
}
