package com.cardgame.logic.potions;
import com.cardgame.logic.GameState;
import com.cardgame.logic.monsters.AbstractMonster;

public class VialOfAcid extends AbstractPotion {
    public VialOfAcid() {
        super("vial_of_acid", "Vial of Acid", "Deal 20 damage to an enemy.", "IMAGES/play/potion2.png");
    }
    @Override
    public boolean isTargeted() { return true; }
    @Override
    public void use(GameState state, AbstractMonster target) {
        if (target != null) target.damage(20);
    }
    @Override
    public AbstractPotion makeCopy() { return new VialOfAcid(); }
}
