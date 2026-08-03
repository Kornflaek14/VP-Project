package com.cardgame.logic.abilities.impl;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.abilities.Ability;

public class AirborneAbility implements Ability {
    @Override
    public boolean canAttackDirectly(CardInstance attacker, CardInstance defender) {
        if (defender == null) return true;
        // Airborne ignores the defender UNLESS the defender has Mighty Leap
        return !defender.getTemplate().abilityIds().contains("mighty_leap");
    }
}
