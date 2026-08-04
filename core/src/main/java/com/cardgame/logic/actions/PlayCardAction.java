package com.cardgame.logic.actions;

import com.cardgame.data.CardType;
import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.AbilityRegistry;
import com.cardgame.logic.events.CardPlayedEvent;
import com.cardgame.logic.events.GameEvent;

import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Plays a card from a player's hand.
 */
public final class PlayCardAction implements GameAction {

    private final int          playerIndex;
    private final CardInstance card;
    private final int          boardPosition; // -1 = append
    private final CardInstance target;        // Can be null

    public PlayCardAction(int playerIndex, CardInstance card, int boardPosition, CardInstance target) {
        this.playerIndex   = playerIndex;
        this.card          = card;
        this.boardPosition = boardPosition;
        this.target        = target;
    }

    @Override
    public List<GameEvent> execute(GameState state) {
        List<GameEvent> events = new ArrayList<>();
        GameState.PlayerState ps = state.getPlayer(playerIndex);

        // ── Validate ──────────────────────────────────────────────────────────
        int boneCost = card.getTemplate().boneCost();
        if (ps.bones < boneCost) {
            throw new IllegalStateException(
                    String.format("Not enough bones to play '%s' (need %d, have %d)",
                            card.getTemplate().name(), boneCost, ps.bones));
        }

        if (!ps.hand.contains(card)) {
            throw new IllegalStateException(
                    "Card '" + card.getTemplate().name() + "' is not in player " + playerIndex + "'s hand");
        }
        
        int bloodCost = card.getTemplate().bloodCost();
        if (bloodCost > 0 && ps.sacrificeCredit < bloodCost) {
            throw new IllegalStateException("Not enough blood (sacrifices). Need " + bloodCost + ", have " + ps.sacrificeCredit);
        }

        boolean isUnit = card.getTemplate().cardType() == CardType.UNIT;
        
        int pos = -1;
        if (isUnit) {
            if (boardPosition >= 0 && boardPosition < Constants.MAX_BOARD_SIZE) {
                if (ps.board[boardPosition] != null) {
                    throw new IllegalStateException("Slot " + boardPosition + " is already occupied.");
                }
                pos = boardPosition;
            } else {
                // Find first empty slot
                for (int i = 0; i < Constants.MAX_BOARD_SIZE; i++) {
                    if (ps.board[i] == null) {
                        pos = i;
                        break;
                    }
                }
                if (pos == -1) {
                    throw new IllegalStateException("Board is full — cannot play another minion");
                }
            }
        }

        // ── Mutate state ──────────────────────────────────────────────────────
        if (boneCost > 0) {
            ps.bones -= boneCost;
        }
        if (bloodCost > 0) {
            ps.sacrificeCredit -= bloodCost;
        }

        ps.hand.remove(card);

        if (isUnit) {
            ps.board[pos] = card;
        } else {
            // Spells/items go to dead pool (reshuffle into deck when deck is empty)
            ps.deadPool.add(card.getTemplate());
        }

        events.add(new CardPlayedEvent(playerIndex, card, pos)); // pos=-1 for spells

        // ── Apply predefined status effects (if any) ──────────────────────────
        if (isUnit) {
            card.getTemplate().statusEffects().forEach(effect -> 
                com.cardgame.logic.StatusEffectProcessor.applyEffect(state, card, effect)
            );
        }

        // ── Trigger onPlay abilities ──────────────────────────────
        for (String abilityId : card.getTemplate().abilityIds()) {
            AbilityRegistry.getInstance()
                           .get(abilityId)
                           .ifPresent(a -> events.addAll(a.onPlayTargeted(card, target, state)));
        }

        return events;
    }
}
