package com.cardgame.logic.actions;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.abilities.Ability;
import com.cardgame.logic.abilities.AbilityRegistry;
import com.cardgame.logic.events.CardPlayedEvent;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.events.ManaChangedEvent;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Plays a card from a player's hand onto their board.
 *
 * <pre>
 * new PlayCardAction(playerIndex, cardInstance, -1).execute(state);
 * </pre>
 *
 * A {@code boardPosition} of {@code -1} appends the card to the end of the board.
 */
public final class PlayCardAction implements GameAction {

    private final int          playerIndex;
    private final CardInstance card;
    private final int          boardPosition; // -1 = append

    public PlayCardAction(int playerIndex, CardInstance card, int boardPosition) {
        this.playerIndex   = playerIndex;
        this.card          = card;
        this.boardPosition = boardPosition;
    }

    @Override
    public List<GameEvent> execute(GameState state) {
        List<GameEvent> events = new ArrayList<>();
        GameState.PlayerState ps = state.getPlayer(playerIndex);

        // ── Validate ──────────────────────────────────────────────────────────
        int cost = card.getTemplate().manaCost();

        if (ps.mana < cost) {
            throw new IllegalStateException(
                    String.format("Not enough mana to play '%s' (need %d, have %d)",
                            card.getTemplate().name(), cost, ps.mana));
        }
        if (!ps.hand.contains(card)) {
            throw new IllegalStateException(
                    "Card '" + card.getTemplate().name() + "' is not in player " + playerIndex + "'s hand");
        }
        if (ps.board.size() >= Constants.MAX_BOARD_SIZE) {
            throw new IllegalStateException("Board is full — cannot play another minion");
        }

        // ── Mutate mana ───────────────────────────────────────────────────────
        ps.mana -= cost;
        events.add(new ManaChangedEvent(playerIndex, ps.mana, ps.maxMana));

        // ── Move card: hand → board ───────────────────────────────────────────
        ps.hand.remove(card);

        int pos = (boardPosition < 0 || boardPosition >= ps.board.size())
                  ? ps.board.size()
                  : boardPosition;
        ps.board.add(pos, card);

        events.add(new CardPlayedEvent(playerIndex, card, pos));

        // ── Trigger onPlay abilities (Battlecry) ──────────────────────────────
        for (String abilityId : card.getTemplate().abilityIds()) {
            AbilityRegistry.getInstance()
                           .get(abilityId)
                           .ifPresent(a -> events.addAll(a.onPlay(card, state)));
        }

        return events;
    }
}
