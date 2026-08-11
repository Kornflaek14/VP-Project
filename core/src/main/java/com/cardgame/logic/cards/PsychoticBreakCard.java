package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class PsychoticBreakCard extends AbstractCard {
    public PsychoticBreakCard() {
        super("PsychoticBreakCard", "Psychotic Break", 0, "The Patient", 14, 0, "Playable only if every card in your hand is an Attack. Deal 14 damage.", "IMAGES/cards/Ironclad/Clash.png", CardType.ATTACK);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
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
        return new PsychoticBreakCard();
    }
}
