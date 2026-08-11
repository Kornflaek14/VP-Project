package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class IntrusiveThoughtCard extends AbstractCard {
    public IntrusiveThoughtCard() {
        super("IntrusiveThoughtCard", "Intrusive Thought", 1, "The Patient", 9, 0, "Deal 9 damage. Put a card from your discard pile on top of your draw pile.", "IMAGES/cards/Ironclad/Headbutt.png", CardType.ATTACK);
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
        return new IntrusiveThoughtCard();
    }
}
