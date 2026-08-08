package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class ViciousStrikeCard extends AbstractCard {
    public ViciousStrikeCard() {
        super("ViciousStrikeCard", "Vicious Strike", 1, "The Patient", 9, 0, "Deal 9 damage and draw 1 card.", "IMAGES/cards/Ironclad/PommelStrike.png", CardType.ATTACK);
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
        return new ViciousStrikeCard();
    }
}
