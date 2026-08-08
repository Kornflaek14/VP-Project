package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.data.StatusEffect;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class SkullCrackCard extends AbstractCard {
    public SkullCrackCard() {
        super("SkullCrackCard", "Pipe", 2, "The Patient", 9, 0, "Deal 9 damage. Apply 2 Paranoid.", "cards/Pipe.png", CardType.ATTACK);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        dealDamage(state, target, damage(), events);
        target.status.apply(StatusEffect.VULNERABLE, 2);
        events.add(new StatusEffectAppliedEvent("monster", StatusEffect.VULNERABLE, 2));
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
        return new SkullCrackCard();
    }
}
