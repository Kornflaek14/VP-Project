package com.cardgame.logic;

import com.cardgame.data.CardData;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.events.GameOverEvent;
import com.cardgame.utils.Constants;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Authoritative snapshot of every piece of game state.
 * <p>
 * Two {@link PlayerState} objects (indexed 0 and 1) hold all per-player data.
 * An event queue accumulates {@link GameEvent}s that are drained by
 * {@code BattleScreen} each render frame.
 * <p>
 * HARD RULE: no libGDX imports.
 */
public final class GameState {

    // ── Inner type ─────────────────────────────────────────────────────────────

    /**
     * All runtime data for one player (hand, board, deck, mana, health).
     */
    public static final class PlayerState {
        public final List<CardInstance> hand        = new ArrayList<>();
        public final CardInstance[]     board       = new CardInstance[Constants.MAX_BOARD_SIZE];
        public final List<CardData>     deck        = new ArrayList<>();
        public final List<CardData>     discardPile = new ArrayList<>();
        public int bones           = 0;
        public int sacrificeCredit = 0; // consumed when playing a card with bloodCost
    }

    // ── Fields ─────────────────────────────────────────────────────────────────

    private final PlayerState[] players;
    private int currentPlayer = 0;  // whose turn it is (0 or 1)
    
    // Scale starts balanced at 0. Positive is Player 0 advantage. Negative is Player 1 advantage.
    private int scaleBalance = 0; 

    /** Events produced by actions; drained by the rendering layer each frame. */
    private final Deque<GameEvent> eventQueue = new ArrayDeque<>();

    // ── Constructor ────────────────────────────────────────────────────────────

    public GameState() {
        players = new PlayerState[]{new PlayerState(), new PlayerState()};
    }

    // ── Player accessors ───────────────────────────────────────────────────────

    public PlayerState getPlayer(int index) {
        if (index < 0 || index >= players.length)
            throw new IndexOutOfBoundsException("Player index must be 0 or 1, got: " + index);
        return players[index];
    }

    public int getCurrentPlayer() { return currentPlayer; }

    public void setCurrentPlayer(int player) {
        if (player < 0 || player >= players.length)
            throw new IndexOutOfBoundsException("Player index must be 0 or 1, got: " + player);
        currentPlayer = player;
    }

    // ── Convenience wrappers ───────────────────────────────────────────────────

    public List<CardInstance> getHand(int player)  { return getPlayer(player).hand;  }
    public CardInstance[] getBoard(int player)     { return getPlayer(player).board; }
    public int getBones(int player)                { return getPlayer(player).bones; }
    public void setBones(int player, int bones)    { getPlayer(player).bones = bones; }

    public int getScaleBalance()                   { return scaleBalance; }
    public void setScaleBalance(int scaleBalance)  { this.scaleBalance = scaleBalance; }

    /**
     * Checks whether the scale has tipped far enough to end the game.
     *
     * <p>Per Inscryption Act 1 rules: a win is triggered the instant the
     * scale reaches ±{@link Constants#WINNING_SCALE_THRESHOLD} points.
     * Player 0 wins on +{@value Constants#WINNING_SCALE_THRESHOLD},
     * Player 1 wins on -{@value Constants#WINNING_SCALE_THRESHOLD}.
     *
     * @return an {@link Optional} containing the {@link GameOverEvent} if the
     *         game should end, or {@link Optional#empty()} if it continues.
     */
    public Optional<GameOverEvent> checkWinCondition() {
        if (scaleBalance >= Constants.WINNING_SCALE_THRESHOLD) {
            return Optional.of(new GameOverEvent(0)); // player 0 wins
        } else if (scaleBalance <= -Constants.WINNING_SCALE_THRESHOLD) {
            return Optional.of(new GameOverEvent(1)); // player 1 (opponent) wins
        }
        return Optional.empty();
    }

    // ── Event queue ────────────────────────────────────────────────────────────

    /** Push a single event into the queue (called by actions and the TurnManager). */
    public void pushEvent(GameEvent event) {
        eventQueue.addLast(event);
    }

    /** Push a batch of events (returned from a {@link com.cardgame.logic.actions.GameAction}). */
    public void pushEvents(List<GameEvent> events) {
        eventQueue.addAll(events);
    }

    /**
     * Poll the oldest event, or {@code null} if the queue is empty.
     * Called by {@code BattleScreen.render()} each frame.
     */
    public GameEvent pollEvent() {
        return eventQueue.pollFirst();
    }

    /** Returns {@code true} if there are unprocessed events pending. */
    public boolean hasEvents() {
        return !eventQueue.isEmpty();
    }

    // ── Utility ────────────────────────────────────────────────────────────────

    /**
     * Finds which player (0 or 1) currently has {@code card} on their board.
     *
     * @return the player index, or -1 if the card is not on any board.
     */
    public int findBoardOwner(CardInstance card) {
        for (int i = 0; i < players.length; i++) {
            for (CardInstance c : players[i].board) {
                if (c == card) return i;
            }
        }
        return -1;
    }

    public boolean removeCardFromBoard(CardInstance card) {
        for (int i = 0; i < players.length; i++) {
            for (int j = 0; j < players[i].board.length; j++) {
                if (players[i].board[j] == card) {
                    players[i].board[j] = null;
                    return true;
                }
            }
        }
        return false;
    }
}
