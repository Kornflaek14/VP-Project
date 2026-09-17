package com.cardgame.logic.potions;
import com.cardgame.logic.GameState;
import com.cardgame.logic.monsters.AbstractMonster;

public class AdrenalineSyringe extends AbstractPotion {
    public AdrenalineSyringe() {
        super("adrenaline_syringe", "Block Potion", "Gain 12 Block this turn.", "IMAGES/PotionImages/BlockPotion.png");
    }
    @Override
    public boolean isTargeted() { return false; }
    @Override
    public void use(GameState state, AbstractMonster target) {
        if (state != null) state.playerBlock += 12;
    }
    @Override
    public AbstractPotion makeCopy() { return new AdrenalineSyringe(); }
}
