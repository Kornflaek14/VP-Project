package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class ManifestTraumaCard extends AbstractCard {
    public ManifestTraumaCard() {
        super("ManifestTraumaCard", "Manifest Trauma", 1, "The Patient", 0, 0, "Deal damage equal to your current Sanity.", "IMAGES/cards/Ironclad/BodySlam.png", CardType.ATTACK);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        dealDamage(state, target, state.playerBlock, events);
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
        return new ManifestTraumaCard();
    }
}
