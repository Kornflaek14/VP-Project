package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.data.StatusEffect;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class ManicBurstCard extends AbstractCard {
    public ManicBurstCard() {
        super("ManicBurstCard", "Manic Burst", 0, "The Patient", 0, 0, "Gain 2 Mania; lose 2 Mania at the end of the turn.", "IMAGES/cards/Ironclad/Flex.png", CardType.SKILL);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        state.playerStatus.apply(StatusEffect.STRENGTH, 2);
        events.add(new StatusEffectAppliedEvent("player", StatusEffect.STRENGTH, 2));
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
        return new ManicBurstCard();
    }
}
