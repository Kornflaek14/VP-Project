package com.cardgame.logic.abilities.impl;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.events.GameEvent;

import java.util.List;

public final class GiveBloodAbility implements Ability {
    private final int amount;
    
    public GiveBloodAbility(int amount) {
        this.amount = amount;
    }
    
    @Override
    public List<GameEvent> onPlayTargeted(CardInstance source, CardInstance target, GameState state) {
        int owner = state.findBoardOwner(source);
        if (owner == -1) owner = source.getOwnerIndex();
        
        state.getPlayer(owner).sacrificeCredit += amount;
        
        return List.of();
    }
}
