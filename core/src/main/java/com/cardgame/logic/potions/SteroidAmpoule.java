package com.cardgame.logic.potions;
import com.cardgame.logic.GameState;
import com.cardgame.logic.monsters.AbstractMonster;
import com.cardgame.data.StatusEffect;

public class SteroidAmpoule extends AbstractPotion {
    public SteroidAmpoule() {
        super("steroid_ampoule", "Strength Potion", "Gain 2 Strength for this combat.", "IMAGES/PotionImages/StrengthPotion.png");
    }
    @Override
    public boolean isTargeted() { return false; }
    @Override
    public void use(GameState state, AbstractMonster target) {
        if (state != null) state.playerStatus.apply(StatusEffect.STRENGTH, 2);
    }
    @Override
    public AbstractPotion makeCopy() { return new SteroidAmpoule(); }
}
