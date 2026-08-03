package com.cardgame.logic.abilities.impl;

import com.cardgame.data.CardData;
import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.events.CardDrawnEvent;
import com.cardgame.logic.events.GameEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>Deathrattle: Draw a card</b> — when this minion dies, its owner draws
 * the top card of their deck (if any remain).
 */
public final class DeathrattleDrawAbility implements Ability {

    @Override
    public List<GameEvent> onDeath(CardInstance source, GameState state) {
        List<GameEvent> events = new ArrayList<>();
        int owner = source.getOwnerIndex();
        GameState.PlayerState ps = state.getPlayer(owner);

        if (!ps.deck.isEmpty()) {
            CardData drawn = ps.deck.remove(0);
            CardInstance instance = new CardInstance(drawn, owner);

            if (ps.hand.size() < com.cardgame.utils.Constants.MAX_HAND_SIZE) {
                ps.hand.add(instance);
                events.add(new CardDrawnEvent(owner, instance));
            }
            // If hand is full, card is burned (no event emitted — a future
            // BurnedEvent can be added here without breaking existing code)
        }
        return events;
    }
}
