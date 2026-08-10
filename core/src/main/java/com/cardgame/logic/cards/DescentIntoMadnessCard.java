package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class DescentIntoMadnessCard extends AbstractCard {
    public DescentIntoMadnessCard() {
        super("DescentIntoMadnessCard", "Descent into Madness", 1, "The Patient", 0, 0, "Play the top card of your draw pile and Exhaust it.", "IMAGES/cards/Ironclad/Havoc.png", CardType.SKILL);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        // Logic implemented later
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
        return new DescentIntoMadnessCard();
    }
}
