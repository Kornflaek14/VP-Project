package com.cardgame.logic.abilities.impl;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.events.GameEvent;

import java.util.List;

public final class RangedAbility implements Ability {
    @Override
    public List<GameEvent> onAttack(CardInstance source, CardInstance target, GameState state) {
        source.setRangedThisTurn(true);
        return List.of();
    }
}
