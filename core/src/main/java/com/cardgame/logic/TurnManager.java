package com.cardgame.logic;

import com.cardgame.data.CardData;
import com.cardgame.logic.abilities.AbilityRegistry;
import com.cardgame.logic.events.*;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase state machine that advances game turns.
 *
 * <p>Phases per turn (currently simplified for 1v1 card play):
 * <ol>
 *   <li>{@link Phase#DRAW}   — active player draws a card and refreshes mana</li>
 *   <li>{@link Phase#MAIN}   — active player plays cards and attacks</li>
 *   <li>{@link Phase#END}    — cleanup; control passes to the other player</li>
 * </ol>
 *
 * HARD RULE: no libGDX imports.
 */
public final class TurnManager {

    /** Game-phase enum visible to the UI for showing phase indicators. */
    public enum Phase { DRAW, MAIN, END }

    private Phase currentPhase = Phase.DRAW;
    private int   turnNumber   = 1;

    // ── Accessors ──────────────────────────────────────────────────────────────

    public Phase getCurrentPhase() { return currentPhase; }
    public int   getTurnNumber()   { return turnNumber;   }

    // ── API ────────────────────────────────────────────────────────────────────

    /**
     * Called once at game start: gives Player 0 their starting mana (1) and
     * draws their opening hand.
     */
    public List<GameEvent> startGame(GameState state) {
        List<GameEvent> events = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            GameState.PlayerState ps = state.getPlayer(i);
            ps.maxMana = (i == 0) ? 1 : 0; // player 0 goes first with 1 mana
            ps.mana    = ps.maxMana;
            events.add(new ManaChangedEvent(i, ps.mana, ps.maxMana));

            // Draw starting hand
            int draws = Constants.STARTING_HAND_SIZE;
            for (int d = 0; d < draws && !ps.deck.isEmpty(); d++) {
                CardData drawn = ps.deck.remove(0);
                CardInstance ci = new CardInstance(drawn, i);
                ps.hand.add(ci);
                events.add(new CardDrawnEvent(i, ci));
            }
        }

        events.add(new TurnChangedEvent(state.getCurrentPlayer(), turnNumber));
        currentPhase = Phase.MAIN;
        return events;
    }

    /**
     * Ends the active player's turn, switches to the opponent, refreshes
     * their mana, un-exhausts their minions, draws one card, and triggers
     * {@code onTurnStart} abilities.
     */
    public List<GameEvent> endTurn(GameState state) {
        List<GameEvent> events = new ArrayList<>();

        // ── Switch active player ───────────────────────────────────────────────
        int next = 1 - state.getCurrentPlayer();
        state.setCurrentPlayer(next);
        turnNumber++;

        // ── Refresh mana ───────────────────────────────────────────────────────
        GameState.PlayerState ps = state.getPlayer(next);
        ps.maxMana = Math.min(Constants.MAX_MANA, ps.maxMana + 1);
        ps.mana    = ps.maxMana;
        events.add(new ManaChangedEvent(next, ps.mana, ps.maxMana));

        // ── Un-exhaust minions ─────────────────────────────────────────────────
        ps.board.forEach(c -> c.setExhausted(false));

        // ── Draw a card ────────────────────────────────────────────────────────
        if (!ps.deck.isEmpty()) {
            CardData drawn = ps.deck.remove(0);
            CardInstance ci = new CardInstance(drawn, next);
            if (ps.hand.size() < Constants.MAX_HAND_SIZE) {
                ps.hand.add(ci);
                events.add(new CardDrawnEvent(next, ci));
            }
            // Otherwise card is burned (fatigue / overdraw not modelled yet)
        }

        events.add(new TurnChangedEvent(next, turnNumber));

        // ── onTurnStart ability hooks ──────────────────────────────────────────
        for (CardInstance ci : new ArrayList<>(ps.board)) { // snapshot to avoid ConcurrentModification
            for (String id : ci.getTemplate().abilityIds()) {
                AbilityRegistry.getInstance()
                               .get(id)
                               .ifPresent(a -> events.addAll(a.onTurnStart(ci, state)));
            }
        }

        currentPhase = Phase.MAIN;
        return events;
    }
}
