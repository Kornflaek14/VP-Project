package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class DenyRealityCard extends AbstractCard {
    public DenyRealityCard() {
        super("DenyRealityCard", "Cower", 1, "The Patient", 0, 4, "Gain 4 Sanity.", "cards/Cower.png", CardType.SKILL);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        gainBlock(state, defence(), events);
        return events;
    }
    
    @Override
    public void upgrade() {
        if (!upgraded) {
            super.upgrade();
            baseBlock += 3;
        }
    }
    @Override
    public AbstractCard makeCopy() {
        return new DenyRealityCard();
    }
}
