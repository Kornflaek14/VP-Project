package com.cardgame.logic;

import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.logic.events.*;
import com.cardgame.logic.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * STS turn flow:
 * 1. Start turn: reset block, refill energy, draw 5 cards
 * 2. Player plays cards (main phase)
 * 3. End turn: discard hand, monster acts, start new turn
 */
public final class TurnManager {

    public static final int CARDS_PER_TURN = 5;

    private int turnNumber = 0;

    public int getTurnNumber() { return turnNumber; }

    /**
     * Start the combat: draw initial hand.
     */
    public List<GameEvent> startCombat(GameState state) {
        List<GameEvent> events = new ArrayList<>();
        turnNumber = 1;
        state.setTurnNumber(turnNumber);
        state.setPlayerTurn(true);
        RunManager.getInstance().getRelics().forEach(r -> r.atBattleStart(state));
        // Relic hook
        RunManager.getInstance().getRelics().forEach(r -> r.atBattleStart(state));

        // Draw initial hand
        events.addAll(drawCards(state, CARDS_PER_TURN));
        events.add(new TurnChangedEvent(turnNumber, true));
        
        return events;
    }

    /**
     * End the player's turn.
     * 1. Discard remaining hand
     * 2. Monster acts
     * 3. Start new player turn
     */
    public List<GameEvent> endPlayerTurn(GameState state) {
        List<GameEvent> events = new ArrayList<>();

        // 1. Discard entire hand
        state.discardPile.addAll(state.hand);
        state.hand.clear();

        // 2. Monster acts
        state.setPlayerTurn(false);
        CombatResolver resolver = new CombatResolver();
        events.addAll(resolver.executeMonsterTurn(state));

        // Check if game is over
        if (state.playerHp <= 0 || state.monsterGroup.areMonstersBasicallyDead()) {
            return events;
        }

        // 3. Start new player turn
        turnNumber++;
        state.setTurnNumber(turnNumber);
        state.setPlayerTurn(true);
        RunManager.getInstance().getRelics().forEach(r -> r.atTurnStart(state));
        // Relic hook
        RunManager.getInstance().getRelics().forEach(r -> r.atBattleStart(state));

        // Tick poison on both combatants at turn start
        int playerPoison = state.playerStatus.tickPoison();
        if (playerPoison > 0) {
            state.playerHp -= playerPoison;
            events.add(new DamageDealtEvent("poison", "player", playerPoison));
            events.add(new PlayerDamagedEvent(playerPoison));
        }
        for(AbstractMonster m : state.monsterGroup.monsters) {
            int p = m.status.tickPoison();
            if (p > 0) m.damage(p);
            m.status.tickDurationEffects();
        }

        // Decrement duration-based status effects (Vulnerable, Weak)
        state.playerStatus.tickDurationEffects();
        

        // Check if game ended due to poison
        state.checkWinCondition().ifPresent(events::add);
        if (state.playerHp <= 0 || state.monsterGroup.areMonstersBasicallyDead()) {
            return events;
        }

        // Reset player block at start of turn
        state.playerBlock = 0;

        // Reset monster block at start of player turn
        if (state.monsterGroup != null) { for(AbstractMonster m : state.monsterGroup.monsters) m.block = 0; }

        // Refill energy
        state.playerEnergy = state.playerMaxEnergy;

        // Draw new hand
        events.addAll(drawCards(state, CARDS_PER_TURN));
        events.add(new TurnChangedEvent(turnNumber, true));

        return events;
    }

    /**
     * Draw cards from draw pile into hand.
     * If draw pile is empty, shuffle discard pile into draw pile.
     */
    private List<GameEvent> drawCards(GameState state, int count) {
        List<GameEvent> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (state.drawPile.isEmpty()) {
                if (state.discardPile.isEmpty()) break; // truly out of cards
                state.drawPile.addAll(state.discardPile);
                state.discardPile.clear();
                Collections.shuffle(state.drawPile);
            }
            AbstractCard card = state.drawPile.remove(0);
            state.hand.add(card);
            events.add(new CardDrawnEvent(card));
        }
        return events;
    }
}
