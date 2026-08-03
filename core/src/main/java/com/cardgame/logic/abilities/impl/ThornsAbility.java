package com.cardgame.logic.abilities.impl;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.events.GameEvent;

import java.util.List;

public final class ThornsAbility implements Ability {
    private final int damage;
    
    public ThornsAbility(int damage) {
        this.damage = damage;
    }
    
    @Override
    public List<GameEvent> onPlayTargeted(CardInstance source, CardInstance target, GameState state) {
        source.setThornsValue(source.getThornsValue() + damage);
        return List.of();
    }
}
