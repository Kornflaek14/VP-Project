package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.data.StatusEffect;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class ObsessiveStrikeCard extends AbstractCard {
    public ObsessiveStrikeCard() {
        super("ObsessiveStrikeCard", "Obsessive Strike", 2, "The Patient", 14, 0, "Deal 14 damage. Mania affects this card 3 times instead of once.", "IMAGES/cards/Ironclad/HeavyBlade.png", CardType.ATTACK);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        int extra = state.playerStatus.get(StatusEffect.STRENGTH) * 2;
        dealDamage(state, target, damage() + extra, events);
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
        return new ObsessiveStrikeCard();
    }
}
