package com.cardgame.logic.abilities.impl;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.events.DamageDealtEvent;
import com.cardgame.logic.events.GameEvent;

import java.util.ArrayList;
import java.util.List;

public final class SpellDamageAbility implements Ability {
    private final int damage;
    
    public SpellDamageAbility(int damage) {
        this.damage = damage;
    }
    
    @Override
    public List<GameEvent> onPlayTargeted(CardInstance source, CardInstance target, GameState state) {
        if (target != null && !target.isDead()) {
            target.dealDamage(damage);
            List<GameEvent> events = new ArrayList<>();
            events.add(new DamageDealtEvent(source, target, damage, false));
            return events;
        }
        return List.of();
    }
}
