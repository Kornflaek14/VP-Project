package com.cardgame.logic;

import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.events.GameOverEvent;

import java.util.*;

/**
 * Authoritative snapshot of a single combat encounter (STS-style).
 *
 * Player has: HP, block, energy, hand, draw pile, discard pile.
 * Monster has: HP, block, intent (next action preview).
 */
public final class GameState {

    // ── Player state ──────────────────────────────────────────
    public int playerHp;
    public int playerMaxHp;
    public int playerBlock;
    public int playerEnergy;
    public int playerMaxEnergy;

    public final List<AbstractCard> hand        = new ArrayList<>();
    public final List<AbstractCard> drawPile    = new ArrayList<>();
    public final List<AbstractCard> discardPile = new ArrayList<>();

    // ── Monster state ─────────────────────────────────────────
    public com.cardgame.logic.monsters.MonsterGroup monsterGroup;
    
    

    // Monster intent for current turn
     // "ATTACK" or "DEFEND"
    

    // Monster stat ranges (used to randomize intent)
    
    

    // ── Status effects ────────────────────────────────────────
    public final StatusEffectState playerStatus  = new StatusEffectState();
    

    // ── Turn tracking ─────────────────────────────────────────
    private int turnNumber = 0;
    private boolean playerTurn = true;

    // ── Event queue ───────────────────────────────────────────
    private final Deque<GameEvent> eventQueue = new ArrayDeque<>();

    // ── Setup ─────────────────────────────────────────────────

    public void initPlayer(int hp, int maxHp, int energy, List<AbstractCard> deck) {
        this.playerHp = hp;
        this.playerMaxHp = maxHp;
        this.playerBlock = 0;
        this.playerEnergy = energy;
        this.playerMaxEnergy = energy;
        this.drawPile.clear();
        this.drawPile.addAll(deck);
        Collections.shuffle(this.drawPile);
        this.hand.clear();
        this.discardPile.clear();
        this.playerStatus.clear();
    }

    public void initMonsters(com.cardgame.logic.monsters.MonsterGroup group) {
        this.monsterGroup = group;
        for(com.cardgame.logic.monsters.AbstractMonster m : monsterGroup.monsters) {
            m.rollMove();
        }
    }

    // ── Accessors ─────────────────────────────────────────────

    public int  getTurnNumber()  { return turnNumber;  }
    public void setTurnNumber(int n) { turnNumber = n; }
    public boolean isPlayerTurn(){ return playerTurn;   }
    public void setPlayerTurn(boolean b) { playerTurn = b; }

    // ── Monster intent ────────────────────────────────────────

    // ── Win condition ─────────────────────────────────────────

    public Optional<GameOverEvent> checkWinCondition() {
        if (monsterGroup != null && monsterGroup.areMonstersBasicallyDead()) return Optional.of(new GameOverEvent(0));
        if (playerHp  <= 0) return Optional.of(new GameOverEvent(1));
        return Optional.empty();
    }

    // ── Event queue ───────────────────────────────────────────

    public void pushEvent(GameEvent e)        { eventQueue.addLast(e);  }
    public void pushEvents(List<GameEvent> e) { eventQueue.addAll(e);   }
    public GameEvent pollEvent()              { return eventQueue.pollFirst(); }
    public boolean hasEvents()                { return !eventQueue.isEmpty();  }
}
