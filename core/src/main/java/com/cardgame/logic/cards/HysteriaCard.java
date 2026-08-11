package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class HysteriaCard extends AbstractCard {
    public HysteriaCard() {
        super("HysteriaCard", "Hysteria", 0, "The Patient", 0, 0, "Draw 1 card, then place a card from your hand on top of your draw pile. Exhaust.", "IMAGES/cards/Ironclad/Warcry.png", CardType.SKILL);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        
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
        return new HysteriaCard();
    }
}
