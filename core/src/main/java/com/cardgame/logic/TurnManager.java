package com.cardgame.logic;

import com.cardgame.data.CardData;
import com.cardgame.logic.abilities.AbilityRegistry;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.events.TurnChangedEvent;
import com.cardgame.logic.events.CardDrawnEvent;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Phase state machine that advances game turns.
 *
 * <p>New flow (one move per turn):
 * <ol>
 *   <li>Player places ONE card (or performs one sacrifice+play).</li>
 *   <li>{@link #onMoveMade} is called immediately after.</li>
 *   <li>Combat resolves for the current player.</li>
 *   <li>Control switches to the other player; hand refills to 6.</li>
 * </ol>
 *
 * HARD RULE: no libGDX imports.
 */
public final class TurnManager {

    /** Game-phase enum visible to the UI for showing phase indicators. */
    public enum Phase { DRAW, MAIN, COMBAT, END }

    private Phase currentPhase = Phase.DRAW;
    private int   turnNumber   = 1;

    // ── Accessors ──────────────────────────────────────────────────────────────

    public Phase getCurrentPhase() { return currentPhase; }
    public int   getTurnNumber()   { return turnNumber;   }

    // ── API ────────────────────────────────────────────────────────────────────

    public List<GameEvent> startGame(GameState state) {
        List<GameEvent> events = new ArrayList<>();

        // Player 0 gets a normal hand
        drawCardsTo(0, Constants.STARTING_HAND_SIZE, state, events);
        
        // Player 1 (Leshy) fills his queue
        GameState.PlayerState p1 = state.getPlayer(1);
        for (int i = 0; i < Constants.MAX_BOARD_SIZE; i++) {
            if (!p1.deck.isEmpty()) {
                CardData drawn = p1.deck.remove(0);
                p1.queueBoard[i] = new CardInstance(drawn, 1);
            }
        }

        events.add(new TurnChangedEvent(state.getCurrentPlayer(), turnNumber));
        currentPhase = Phase.MAIN;
        return events;
    }

    /**
     * Called after the active player makes ONE move (play a card).
     * Now allows multiple moves per turn by doing nothing.
     */
    public List<GameEvent> onMoveMade(GameState state) {
        return Collections.emptyList();
    }

    /**
     * Ends the current player's turn.
     * Resolves combat → switches player → refills hand → fires events.
     */
    public List<GameEvent> endTurn(GameState state) {
        List<GameEvent> events = new ArrayList<>();
        int activePlayer = state.getCurrentPlayer();
        
        currentPhase = Phase.COMBAT;
        
        // ── Resolve Combat Phase ───────────────────────────────────────────────
        CombatResolver combatResolver = new CombatResolver();
        events.addAll(combatResolver.resolveCombatPhase(state, activePlayer));

        // ── Switch active player ───────────────────────────────────────────────
        int next = 1 - activePlayer;
        state.setCurrentPlayer(next);
        turnNumber++;

        // ── Process Status Effects ─────────────────────────────────────────────
        StatusEffectProcessor.processTurnStart(state, next);

        // ── Un-exhaust & Reset flags ───────────────────────────────────────────
        GameState.PlayerState ps = state.getPlayer(next);
        for (CardInstance c : ps.board) {
            if (c != null) {
                c.setExhausted(false);
                c.setRangedThisTurn(false);
            }
        }
        
        // Reset sacrifice credits at the end of the turn
        state.getPlayer(0).sacrificeCredit = 0;
        state.getPlayer(1).sacrificeCredit = 0;

        // ── Draw to fill hand to STARTING_HAND_SIZE (Player 0 only) ────────────
        if (next == 0) {
            drawCardsTo(next, Constants.STARTING_HAND_SIZE, state, events);
        }

        events.add(new TurnChangedEvent(next, turnNumber));

        // ── onTurnStart ability hooks ──────────────────────────────────────────
        CardInstance[] boardSnapshot = ps.board.clone(); // snapshot to avoid ConcurrentModification
        for (CardInstance ci : boardSnapshot) {
            if (ci != null) {
                for (String id : ci.getTemplate().abilityIds()) {
                    AbilityRegistry.getInstance()
                                   .get(id)
                                   .ifPresent(a -> events.addAll(a.onTurnStart(ci, state)));
                }
            }
        }

        currentPhase = Phase.MAIN;
        return events;
    }

    /**
     * Executes Leshy's (Player 1) fully automated turn.
     * 1. Advance queue to active board
     * 2. Fill queue from deck
     * 3. Resolve combat
     * 4. Switch turn back to Player 0
     */
    public List<GameEvent> executeLeshyTurn(GameState state) {
        List<GameEvent> events = new ArrayList<>();
        GameState.PlayerState p1 = state.getPlayer(1);

        // 1. Advance Queue
        for (int i = 0; i < Constants.MAX_BOARD_SIZE; i++) {
            if (p1.board[i] == null && p1.queueBoard[i] != null) {
                p1.board[i] = p1.queueBoard[i];
                p1.queueBoard[i] = null;
                events.add(new com.cardgame.logic.events.CardPlayedEvent(1, p1.board[i], i));
            }
        }

        // 2. Fill Queue
        for (int i = 0; i < Constants.MAX_BOARD_SIZE; i++) {
            if (p1.queueBoard[i] == null) {
                if (p1.deck.isEmpty() && !p1.deadPool.isEmpty()) {
                    p1.deck.addAll(p1.deadPool);
                    p1.deadPool.clear();
                    java.util.Collections.shuffle(p1.deck);
                }
                if (!p1.deck.isEmpty()) {
                    CardData drawn = p1.deck.remove(0);
                    // Create instance directly into queue
                    CardInstance ci = new CardInstance(drawn, 1);
                    p1.queueBoard[i] = ci;
                }
            }
        }

        // 3 & 4. Combat & Switch (re-use endTurn for Player 1)
        events.addAll(endTurn(state));
        return events;
    }

    private void drawCardsTo(int playerIndex, int targetHandSize, GameState state, List<GameEvent> events) {
        GameState.PlayerState ps = state.getPlayer(playerIndex);
        while (ps.hand.size() < targetHandSize) {
            if (ps.deck.isEmpty()) {
                // Dead pool reshuffle: only deadPool goes back, NEVER discardPile
                if (!ps.deadPool.isEmpty()) {
                    ps.deck.addAll(ps.deadPool);
                    ps.deadPool.clear();
                    Collections.shuffle(ps.deck);
                } else {
                    break; // Truly out of cards
                }
            }
            CardData drawn = ps.deck.remove(0);
            CardInstance ci = new CardInstance(drawn, playerIndex);
            ps.hand.add(ci);
            events.add(new CardDrawnEvent(playerIndex, ci));
        }
    }
}
