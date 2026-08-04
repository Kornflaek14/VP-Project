package com.cardgame.logic.abilities.impl;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.events.PlayerDamagedEvent;
import com.cardgame.utils.Constants;

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

        // Heal tips the scale in the owning player's favor
        int sign = (owner == 0) ? 1 : -1;
        state.addScaleBalance(amount * sign);

        List<GameEvent> events = new ArrayList<>();
        // Emit a negative damage event to trigger visual feedback
        events.add(new PlayerDamagedEvent(owner, -amount));
        state.checkWinCondition().ifPresent(events::add);
        return events;
    }
}
