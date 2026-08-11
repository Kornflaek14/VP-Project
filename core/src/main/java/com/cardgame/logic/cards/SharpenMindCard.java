package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class SharpenMindCard extends AbstractCard {
    public SharpenMindCard() {
        super("SharpenMindCard", "Sharpen Mind", 1, "The Patient", 0, 5, "Gain 5 Sanity. Upgrade a card in your hand for the rest of combat.", "IMAGES/cards/Ironclad/Armaments.png", CardType.SKILL);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        gainBlock(state, defence(), events);
        
        // Upgrade a random un-upgraded card in hand for the rest of combat
        List<AbstractCard> upgradableCards = new ArrayList<>();
        for (AbstractCard c : state.hand) {
            if (!c.isUpgraded()) upgradableCards.add(c);
        }
        if (!upgradableCards.isEmpty()) {
            AbstractCard toUpgrade = upgradableCards.get(new java.util.Random().nextInt(upgradableCards.size()));
            toUpgrade.upgrade();
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
        return new SharpenMindCard();
    }
}
