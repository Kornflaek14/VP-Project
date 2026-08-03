package com.cardgame.logic.abilities.impl;

import com.cardgame.data.StatusEffectData;
import com.cardgame.data.StatusEffectType;
import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.StatusEffectProcessor;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.events.GameEvent;

import java.util.List;

public final class AreaFreezeAbility implements Ability {
    private final int duration;
    
    public AreaFreezeAbility(int duration) {
        this.duration = duration;
    }
    
    @Override
    public List<GameEvent> onPlayTargeted(CardInstance source, CardInstance target, GameState state) {
        int opponentIndex = 1 - source.getOwnerIndex();
        CardInstance[] enemyBoard = state.getBoard(opponentIndex);
        for (CardInstance enemy : enemyBoard) {
            if (enemy != null) {
                StatusEffectProcessor.applyEffect(state, enemy, new StatusEffectData(StatusEffectType.FREEZE, 0, duration));
            }
        }
        return List.of();
    }
}
