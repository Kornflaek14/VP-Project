package com.cardgame.logic;

import com.cardgame.data.CardData;
import com.cardgame.logic.abilities.AbilityRegistry;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.events.TurnChangedEvent;
import com.cardgame.logic.events.CardDrawnEvent;
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
    public enum Phase { DRAW, MAIN, COMBAT, END }

    private Phase currentPhase = Phase.DRAW;
    private int   turnNumber   = 1;

    // ── Accessors ──────────────────────────────────────────────────────────────

    public Phase getCurrentPhase() { return currentPhase; }
    public int   getTurnNumber()   { return turnNumber;   }

    // ── API ────────────────────────────────────────────────────────────────────

    public List<GameEvent> startGame(GameState state) {
        List<GameEvent> events = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            // Draw up to 4 cards
            drawCardsTo(i, Constants.STARTING_HAND_SIZE, state, events);
        }

        events.add(new TurnChangedEvent(state.getCurrentPlayer(), turnNumber));
        currentPhase = Phase.MAIN;
        return events;
    }

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

        // ── Draw up to 4 cards ─────────────────────────────────────────────────
        drawCardsTo(next, Constants.STARTING_HAND_SIZE, state, events);

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

    private void drawCardsTo(int playerIndex, int targetHandSize, GameState state, List<GameEvent> events) {
        GameState.PlayerState ps = state.getPlayer(playerIndex);
        while (ps.hand.size() < targetHandSize) {
            if (ps.deck.isEmpty()) {
                if (ps.discardPile.isEmpty()) {
                    break; // No cards left anywhere
                }
                // Reshuffle discard into deck
                ps.deck.addAll(ps.discardPile);
                ps.discardPile.clear();
                java.util.Collections.shuffle(ps.deck);
            }
            CardData drawn = ps.deck.remove(0);
            CardInstance ci = new CardInstance(drawn, playerIndex);
            ps.hand.add(ci);
            events.add(new CardDrawnEvent(playerIndex, ci));
        }
    }
}
