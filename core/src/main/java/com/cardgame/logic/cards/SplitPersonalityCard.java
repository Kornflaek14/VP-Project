package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class SplitPersonalityCard extends AbstractCard {
    public SplitPersonalityCard() {
        super("SplitPersonalityCard", "Split Personality", 1, "The Patient", 5, 0, "Deal 5 damage twice.", "IMAGES/cards/Ironclad/TwinStrike.png", CardType.ATTACK);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        dealDamage(state, target, damage(), events);
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
        return new SplitPersonalityCard();
    }
}
