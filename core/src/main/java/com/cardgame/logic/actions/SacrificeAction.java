package com.cardgame.logic.actions;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.events.SacrificeEvent;
import com.cardgame.logic.events.CardDiedEvent;
import com.cardgame.logic.abilities.AbilityRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Sacrifices N friendlies to gain sacrifice credit for big cards.
 */
public final class SacrificeAction implements GameAction {

    private final int playerIndex;
    private final List<CardInstance> targets;

    public SacrificeAction(int playerIndex, List<CardInstance> targets) {
        this.playerIndex = playerIndex;
        this.targets = targets;
    }

    @Override
    public List<GameEvent> execute(GameState state) {
        List<GameEvent> events = new ArrayList<>();
        GameState.PlayerState ps = state.getPlayer(playerIndex);

        for (CardInstance target : targets) {
            if (!state.removeCardFromBoard(target)) {
                throw new IllegalStateException("Card is not on your board");
            }
            
            ps.sacrificeCredit++;
            ps.bones++; // Inscryption: sacrificing a card gives a bone
            ps.deadPool.add(target.getTemplate()); // goes to dead pool for reshuffling
            events.add(new SacrificeEvent(playerIndex, target));
            events.add(new CardDiedEvent(playerIndex, target));
            
            for (String id : target.getTemplate().abilityIds()) {
                AbilityRegistry.getInstance()
                               .get(id)
                               .ifPresent(a -> events.addAll(a.onDeath(target, state)));
            }
        }

        return events;
    }
}
