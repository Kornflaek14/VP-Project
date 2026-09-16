package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class StubbornDenialCard extends AbstractCard {
    public StubbornDenialCard() {
        super("StubbornDenialCard", "Brick", 1, "The Patient", 0, 7, "Gain 7 Sanity; Exhaust a random card in your hand.", "cards/Brick.png", CardType.SKILL);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        gainBlock(state, defence(), events);
        if (!state.hand.isEmpty()) {
            int randomIndex = ThreadLocalRandom.current().nextInt(state.hand.size());
            AbstractCard exhausted = state.hand.remove(randomIndex);
            state.exhaustPile.add(exhausted);
        }
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
        AbstractCard copy = new StubbornDenialCard();
        if (this.upgraded) copy.upgrade();
        return copy;
    }
}
