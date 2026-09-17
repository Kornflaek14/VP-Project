package com.cardgame.logic.potions;

import com.cardgame.logic.GameState;
import com.cardgame.logic.monsters.AbstractMonster;

public final class ManaPotion extends AbstractPotion {
    public ManaPotion() {
        super("mana_potion", "Mana Potion", "Gain 2 Energy this turn.", "IMAGES/PotionImages/EnergyPotion.png");
    }

    @Override public boolean isTargeted() { return false; }
    @Override public void use(GameState state, AbstractMonster target) {
        if (state != null) state.playerEnergy += 2;
    }
    @Override public AbstractPotion makeCopy() { return new ManaPotion(); }
}
