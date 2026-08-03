package com.cardgame.logic.abilities.impl;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.Ability;

import java.util.ArrayList;
import java.util.List;

public class BifurcatedStrikeAbility implements Ability {
    @Override
    public List<Integer> getAttackTargets(CardInstance attacker, GameState state, int attackerSlot) {
        List<Integer> targets = new ArrayList<>();
        if (attackerSlot > 0) {
            targets.add(attackerSlot - 1);
        }
        if (attackerSlot < com.cardgame.utils.Constants.MAX_BOARD_SIZE - 1) {
            targets.add(attackerSlot + 1);
        }
        return targets;
    }
}
