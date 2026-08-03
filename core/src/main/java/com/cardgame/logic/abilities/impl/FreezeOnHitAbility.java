package com.cardgame.logic.abilities.impl;

import com.cardgame.data.StatusEffectData;
import com.cardgame.data.StatusEffectType;
import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.StatusEffectProcessor;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.events.GameEvent;

import java.util.List;

public final class FreezeOnHitAbility implements Ability {
    private final int duration;
    
    public FreezeOnHitAbility(int duration) {
        this.duration = duration;
    }
    
    @Override
    public List<GameEvent> onAttack(CardInstance source, CardInstance target, GameState state) {
        if (target != null && !target.isDead()) {
            StatusEffectProcessor.applyEffect(state, target, new StatusEffectData(StatusEffectType.FREEZE, 0, duration));
        }
        return List.of();
    }
}
