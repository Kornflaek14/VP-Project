package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class DelusionalStrikeCard extends AbstractCard {
    public DelusionalStrikeCard() {
        super("DelusionalStrikeCard", "Delusional Strike", 2, "The Patient", 6, 0, "Deal 6 damage, plus 2 for each card containing Strike in your deck.", "IMAGES/cards/Ironclad/PerfectedStrike.png", CardType.ATTACK);
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
        return new DelusionalStrikeCard();
    }
}
