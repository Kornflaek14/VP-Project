package com.cardgame.logic.abilities.impl;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.events.ScaleChangedEvent;

import java.util.ArrayList;
import java.util.List;

public final class HealHeroAbility implements Ability {
    private final int amount;
    
    public HealHeroAbility(int amount) {
        this.amount = amount;
    }
    
    @Override
    public List<GameEvent> onPlayTargeted(CardInstance source, CardInstance target, GameState state) {
        int owner = state.findBoardOwner(source);
        if (owner == -1) owner = source.getOwnerIndex(); // Spell not on board yet

        if (owner == 0) {
            state.setScaleBalance(state.getScaleBalance() + amount);
        } else {
            state.setScaleBalance(state.getScaleBalance() - amount);
        }

        List<GameEvent> events = new ArrayList<>();
        events.add(new ScaleChangedEvent(state.getScaleBalance(), amount, owner));
        // Scale may tip to win threshold via a heal spell
        state.checkWinCondition().ifPresent(events::add);
        return events;
    }
}
