package com.cardgame.logic.relics;
import com.cardgame.logic.GameState;
import com.cardgame.data.StatusEffect;

public class RustedScalpel extends AbstractRelic {
    public RustedScalpel() {
        super("rusted_scalpel", "Rusted Scalpel", "Start each combat with 1 Strength.", "IMAGES/play/relic2.png");
    }
    @Override
    public void atBattleStart(GameState state) {
        state.playerStatus.apply(StatusEffect.STRENGTH, 1);
    }
    @Override
    public AbstractRelic makeCopy() { return new RustedScalpel(); }
}
