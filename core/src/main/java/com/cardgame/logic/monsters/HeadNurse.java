package com.cardgame.logic.monsters;

import com.cardgame.data.StatusEffect;
import com.cardgame.logic.GameState;
import java.util.concurrent.ThreadLocalRandom;

public class HeadNurse extends AbstractMonster {
    private int turnCount = 0;
    private String currentAction = "SEDATIVE";

    public HeadNurse(float x, float y) {
        super("Head Nurse", 82, "Character sprite/Enemies/Elite enemy/nurse/idle/Idle.png");
        this.drawX = x;
        this.drawY = y;
        rollMove();
    }

    @Override
    public boolean isElite() {
        return true;
    }

    @Override
    public void rollMove() {
        if (turnCount == 0) {
            currentAction = "SEDATIVE";
            intentType = "ATTACK_DEBUFF";
            intentValue = 9 + status.get(StatusEffect.STRENGTH);
        } else if (turnCount == 1) {
            currentAction = "STABILIZE";
            intentType = "ATTACK_DEFEND";
            intentValue = 7 + status.get(StatusEffect.STRENGTH);
        } else if (turnCount == 2) {
            currentAction = "LETHAL_DOSE";
            intentType = "ATTACK";
            intentValue = 15 + status.get(StatusEffect.STRENGTH);
        } else {
            int roll = ThreadLocalRandom.current().nextInt(100);
            if (roll < 45) {
                currentAction = "SEDATIVE";
                intentType = "ATTACK_DEBUFF";
                intentValue = 9 + status.get(StatusEffect.STRENGTH);
            } else if (roll < 75) {
                currentAction = "LETHAL_DOSE";
                intentType = "ATTACK";
                intentValue = 15 + status.get(StatusEffect.STRENGTH);
            } else {
                currentAction = "STABILIZE";
                intentType = "ATTACK_DEFEND";
                intentValue = 7 + status.get(StatusEffect.STRENGTH);
            }
        }
    }

    @Override
    public void takeTurn(GameState state) {
        if ("SEDATIVE".equals(currentAction)) {
            dealDmg(state, intentValue);
            state.playerStatus.apply(StatusEffect.WEAK, 2);
        } else if ("STABILIZE".equals(currentAction)) {
            dealDmg(state, intentValue);
            block += 10;
            status.apply(StatusEffect.STRENGTH, 1);
        } else if ("LETHAL_DOSE".equals(currentAction)) {
            dealDmg(state, intentValue);
        }
        turnCount++;
        rollMove();
    }

    private void dealDmg(GameState state, int base) {
        int dmg = base;
        if (status.has(StatusEffect.WEAK)) dmg = (int)(dmg * 0.75f);
        if (state.playerStatus.has(StatusEffect.VULNERABLE) || state.playerStatus.has(StatusEffect.PARANOID)) {
            dmg = (int)(dmg * 1.5f);
        }
        if (state.playerBlock >= dmg) {
            state.playerBlock -= dmg;
        } else {
            dmg -= state.playerBlock;
            state.playerBlock = 0;
            state.playerHp -= dmg;
        }
    }
}
