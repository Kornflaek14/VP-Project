package com.cardgame.logic.cards;
import com.cardgame.data.CardType;
import com.cardgame.data.StatusEffect;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.*;
import java.util.ArrayList;
import java.util.List;

public class SuddenPanicCard extends AbstractCard {
    public SuddenPanicCard() {
        super("SuddenPanicCard", "Sudden Panic", 1, "The Patient", 4, 0, "Deal 4 damage to all enemies; apply 1 Paranoid.", "IMAGES/cards/Ironclad/Thunderclap.png", CardType.ATTACK);
    }
    @Override
    public List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target) {
        List<GameEvent> events = new ArrayList<>();
        dealDamage(state, target, damage(), events);
        target.status.apply(StatusEffect.VULNERABLE, 1);
        events.add(new StatusEffectAppliedEvent("monster", StatusEffect.VULNERABLE, 1));
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
        return new SuddenPanicCard();
    }
}
