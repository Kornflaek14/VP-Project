package com.cardgame.logic.abilities.impl;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.events.GameEvent;

import java.util.List;

/**
 * <b>Charge</b> — removes summoning sickness on play so the minion can attack
 * immediately the turn it is summoned.
 */
public final class ChargeAbility implements Ability {

    @Override
    public List<GameEvent> onPlay(CardInstance source, GameState state) {
        source.setExhausted(false); // override the default summoning sickness
        return List.of();
    }
}
