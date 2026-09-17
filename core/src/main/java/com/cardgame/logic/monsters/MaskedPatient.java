package com.cardgame.logic.monsters;

import com.cardgame.data.StatusEffect;
import com.cardgame.logic.GameState;
import java.util.concurrent.ThreadLocalRandom;

public class MaskedPatient extends AbstractMonster {
    private int turnCount = 0;
    private String currentAction = "THRASH";

    public MaskedPatient(float x, float y) {
        super("Masked Patient", 90, "Character sprite/Enemies/Elite enemy/masked/idle/Idle.png");
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
            currentAction = "THRASH";
            intentType = "ATTACK";
            intentValue = 13 + status.get(StatusEffect.STRENGTH);
        } else if (turnCount == 1) {
            currentAction = "FRENZY";
            intentType = "BUFF";
            intentValue = 0;
        } else if (turnCount == 2) {
            currentAction = "FLAIL";
            intentType = "ATTACK";
            intentValue = (7 + status.get(StatusEffect.STRENGTH)) * 2;
        } else {
            int roll = ThreadLocalRandom.current().nextInt(100);
            if (roll < 50) {
                currentAction = "THRASH";
                intentType = "ATTACK";
                intentValue = 13 + status.get(StatusEffect.STRENGTH);
            } else if (roll < 80) {
                currentAction = "HEAVY_SLAM";
                intentType = "ATTACK_DEFEND";
                intentValue = 10 + status.get(StatusEffect.STRENGTH);
            } else {
                currentAction = "FRENZY";
                intentType = "BUFF";
                intentValue = 0;
            }
        }
    }

    @Override
    public void takeTurn(GameState state) {
        if ("FLAIL".equals(currentAction)) {
            int hit = 7 + status.get(StatusEffect.STRENGTH);
            dealDmg(state, hit);
            dealDmg(state, hit);
        } else if ("THRASH".equals(currentAction)) {
            dealDmg(state, intentValue);
        } else if ("HEAVY_SLAM".equals(currentAction)) {
            dealDmg(state, intentValue);
            block += 6;
        } else if ("FRENZY".equals(currentAction)) {
            status.apply(StatusEffect.STRENGTH, 2);
            block += 8;
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
