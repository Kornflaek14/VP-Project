package com.cardgame.logic.monsters;

import com.cardgame.logic.GameState;
import com.cardgame.data.StatusEffect;
import java.util.concurrent.ThreadLocalRandom;

public class Boss extends AbstractMonster {
    private int turnCount = 0;

    public Boss(float x, float y) {
        super("Boss", 160, "Character sprite/Enemies/Boss/idle/idle1.png");
        this.drawX = x;
        this.drawY = y;
        rollMove();
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public void rollMove() {
        turnCount++;
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (turnCount > 0 && turnCount % 3 == 0) {
            // Powerful attack every 3 turns
            intentType = "ATTACK";
            intentValue = 18 + status.get(StatusEffect.STRENGTH);
        } else if (roll < 45) {
            intentType = "ATTACK";
            intentValue = 12 + status.get(StatusEffect.STRENGTH);
        } else if (roll < 75) {
            intentType = "ATTACK_DEFEND";
            intentValue = 8 + status.get(StatusEffect.STRENGTH);
        } else {
            intentType = "BUFF";
            intentValue = 0;
        }
    }

    @Override
    public void takeTurn(GameState state) {
        if (intentType.equals("ATTACK")) {
            dealDmg(state, intentValue);
        } else if (intentType.equals("ATTACK_DEFEND")) {
            dealDmg(state, intentValue);
            block += 8;
        } else if (intentType.equals("BUFF")) {
            status.apply(StatusEffect.STRENGTH, 2);
            block += 12;
        }
        rollMove();
    }

    private void dealDmg(GameState state, int base) {
        int dmg = base;
        if (status.has(StatusEffect.WEAK)) dmg = (int)(dmg * 0.75f);
        if (state.playerBlock >= dmg) {
            state.playerBlock -= dmg;
        } else {
            dmg -= state.playerBlock;
            state.playerBlock = 0;
            state.playerHp -= dmg;
        }
    }
}
