package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class StubbornDenialCard extends AbstractCard {
    public StubbornDenialCard() {
        super("StubbornDenialCard", "Stubborn Denial", 1, "The Patient", 0, 7, "Gain 7 Sanity; Exhaust a random card in your hand.", "IMAGES/cards/Ironclad/TrueGrit.png", CardType.SKILL);
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
        return new StubbornDenialCard();
    }
}
