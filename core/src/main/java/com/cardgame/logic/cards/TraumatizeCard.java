package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.data.StatusEffect;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class TraumatizeCard extends AbstractCard {
    public TraumatizeCard() {
        super("TraumatizeCard", "Traumatize", 2, "The Patient", 12, 0, "Deal 12 damage; apply 2 Despair.", "IMAGES/cards/Ironclad/Clothesline.png", CardType.ATTACK);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        dealDamage(state, target, damage(), events);
        target.status.apply(StatusEffect.WEAK, 2);
        events.add(new StatusEffectAppliedEvent("monster", StatusEffect.WEAK, 2));
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
        return new TraumatizeCard();
    }
}
