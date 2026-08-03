package com.cardgame.logic.abilities.impl;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.events.GameEvent;

import java.util.List;

public final class AoeBuffAtkAbility implements Ability {
    private final int amount;
    
    public AoeBuffAtkAbility(int amount) {
        this.amount = amount;
    }
    
    @Override
    public List<GameEvent> onPlayTargeted(CardInstance source, CardInstance target, GameState state) {
        int owner = source.getOwnerIndex();
        CardInstance[] board = state.getBoard(owner);
        for (CardInstance minion : board) {
            if (minion != null) {
                minion.addAttackBonus(amount);
            }
        }
        return List.of();
    }
}
