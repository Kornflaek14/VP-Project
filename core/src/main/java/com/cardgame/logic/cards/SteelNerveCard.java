package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class SteelNerveCard extends AbstractCard {
    public SteelNerveCard() {
        super("SteelNerveCard", "Steel Nerve", 1, "The Patient", 5, 5, "Gain 5 Sanity and deal 5 damage.", "IMAGES/cards/Ironclad/IronWave.png", CardType.ATTACK);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        gainBlock(state, defence(), events);
        dealDamage(state, target, damage(), events);
        return events;
    }
    
    @Override
    public void upgrade() {
        if (!upgraded) {
            super.upgrade();
            baseDamage += 3;
        }
    }
    @Override
    public AbstractCard makeCopy() {
        return new SteelNerveCard();
    }
}
