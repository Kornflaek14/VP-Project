package com.cardgame.logic.relics;
import com.cardgame.logic.RunManager;

public class TaintedIVBag extends AbstractRelic {
    public TaintedIVBag() {
        super("tainted_iv_bag", "Tainted IV Bag", "Heal 6 HP at the end of combat.", "IMAGES/play/relic1.png");
    }
    @Override
    public void onVictory() {
        RunManager.getInstance().heal(6);
    }
    @Override
    public AbstractRelic makeCopy() { return new TaintedIVBag(); }
}
