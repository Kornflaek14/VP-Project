package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class SeethingHatredCard extends AbstractCard {
    public SeethingHatredCard() {
        super("SeethingHatredCard", "Seething Hatred", 0, "The Patient", 6, 0, "Deal 6 damage. Add a copy of this card to your discard pile.", "IMAGES/cards/Ironclad/Anger.png", CardType.ATTACK);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        dealDamage(state, target, damage(), events);
        state.discardPile.add(this.makeCopy());
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
        return new SeethingHatredCard();
    }
}
