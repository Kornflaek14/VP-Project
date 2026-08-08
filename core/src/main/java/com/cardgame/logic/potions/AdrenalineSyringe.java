package com.cardgame.logic.potions;
import com.cardgame.logic.GameState;
import com.cardgame.logic.monsters.AbstractMonster;

public class AdrenalineSyringe extends AbstractPotion {
    public AdrenalineSyringe() {
        super("adrenaline_syringe", "Adrenaline Syringe", "Gain 12 Block.", "IMAGES/play/potion1.png");
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
