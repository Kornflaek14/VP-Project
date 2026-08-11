package com.cardgame.logic.monsters;
import com.cardgame.logic.GameState;
import com.cardgame.data.StatusEffect;
import java.util.concurrent.ThreadLocalRandom;

public class FleshAmalgam extends AbstractMonster {
    public FleshAmalgam(float x, float y) {
        super("Flesh Amalgam", 42, "IMAGES/play/monster2.png");
        this.drawX = x; this.drawY = y;
    }
    
    @Override
    public void rollMove() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 45) { intentType = "ATTACK"; intentValue = 11; }
        else if (roll < 75) { intentType = "ATTACK_DEFEND"; intentValue = 7; }
        else { intentType = "BUFF"; intentValue = 0; }
    }
    
    @Override
    public void takeTurn(GameState state) {
        if (intentType.equals("ATTACK")) {
            dealDmg(state, intentValue);
        } else if (intentType.equals("ATTACK_DEFEND")) {
            dealDmg(state, intentValue);
            block += 5;
        } else if (intentType.equals("BUFF")) {
            status.apply(StatusEffect.STRENGTH, 2);
            block += 5;
        }
        rollMove();
    }
    
    private void dealDmg(GameState state, int base) {
        int dmg = base + status.get(StatusEffect.STRENGTH);
        if (status.has(StatusEffect.WEAK)) dmg = (int)(dmg * 0.75f);
        if (state.playerBlock >= dmg) { state.playerBlock -= dmg; }
        else { dmg -= state.playerBlock; state.playerBlock = 0; state.playerHp -= dmg; }
    }
}
