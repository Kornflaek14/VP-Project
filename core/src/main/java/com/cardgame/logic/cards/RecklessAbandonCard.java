package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class RecklessAbandonCard extends AbstractCard {
    public RecklessAbandonCard() {
        super("RecklessAbandonCard", "Reckless Abandon", 1, "The Patient", 12, 0, "Deal 12 damage; shuffle a Trauma into your draw pile.", "IMAGES/cards/Ironclad/WildStrike.png", CardType.ATTACK);
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
        return new RecklessAbandonCard();
    }
}
