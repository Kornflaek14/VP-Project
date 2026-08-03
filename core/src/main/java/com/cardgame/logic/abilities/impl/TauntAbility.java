package com.cardgame.logic.abilities.impl;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.events.GameEvent;

import java.util.List;

/**
 * <b>Taunt</b> — when played, marks the minion as a taunt target.
 * The {@link com.cardgame.logic.CombatResolver} enforces that attacking minions
 * must target a taunt minion if one exists on the defending board.
 */
public final class TauntAbility implements Ability {

    @Override
    public List<GameEvent> onPlayTargeted(CardInstance source, CardInstance target, GameState state) {
        source.setTaunt(true);
        return List.of(); // taunt is a passive flag; no event needed
    }
}
