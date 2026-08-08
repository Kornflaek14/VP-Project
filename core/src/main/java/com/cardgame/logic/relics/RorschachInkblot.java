package com.cardgame.logic.relics;
import com.cardgame.logic.GameState;
import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.data.CardType;

public class RorschachInkblot extends AbstractRelic {
    public RorschachInkblot() {
        super("rorschach_inkblot", "Rorschach Inkblot", "Every 10th Attack deals double damage.", "IMAGES/play/relic.png");
        this.counter = 0;
    }
    @Override
    public void onPlayCard(AbstractCard card, GameState state) {
        if (card.cardType() == CardType.ATTACK) {
            counter++;
            if (counter >= 10) {
                // Future impl: actually doubling damage requires passing a buff state.
                // For MVP: reset counter. Actual double damage logic requires modifying AbstractCard dealDamage.
                counter = 0;
            }
        }
    }
    @Override
    public AbstractRelic makeCopy() { return new RorschachInkblot(); }
}
