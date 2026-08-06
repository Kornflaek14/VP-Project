package com.cardgame.logic;

import com.cardgame.data.CardData;
import com.cardgame.data.MonsterData;
import com.cardgame.data.PotionData;
import com.cardgame.data.RelicData;
import com.cardgame.data.StatusEffect;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.events.GameOverEvent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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

    public final List<CardData> hand        = new ArrayList<>();
    public final List<CardData> drawPile    = new ArrayList<>();
    public final List<CardData> discardPile = new ArrayList<>();

    // ── Monster state ─────────────────────────────────────────
    public String monsterName = "";
    public int    monsterHp;
    public int    monsterMaxHp;
    public int    monsterBlock;
    public String monsterImage = "";

    // Monster intent for current turn
    public String intentType = "ATTACK"; // "ATTACK" or "DEFEND"
    public int    intentValue = 0;

    // Monster stat ranges (used to randomize intent)
    private int monsterAtkMin, monsterAtkMax;
    private int monsterDefMin, monsterDefMax;

    // ── Status effects ────────────────────────────────────────
    public final StatusEffectState playerStatus  = new StatusEffectState();
    public final StatusEffectState monsterStatus = new StatusEffectState();

    // ── Turn tracking ─────────────────────────────────────────
    private int turnNumber = 0;
    private boolean playerTurn = true;

    // ── Event queue ───────────────────────────────────────────
    private final Deque<GameEvent> eventQueue = new ArrayDeque<>();

    // ── Setup ─────────────────────────────────────────────────

    public void initPlayer(int hp, int maxHp, int energy, List<CardData> deck) {
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

    public void initMonster(MonsterData data) {
        this.monsterName  = data.name();
        this.monsterHp    = data.hp();
        this.monsterMaxHp = data.hp();
        this.monsterBlock = 0;
        this.monsterImage = data.image();
        this.monsterAtkMin = data.attackMin();
        this.monsterAtkMax = data.attackMax();
        this.monsterDefMin = data.defenceMin();
        this.monsterDefMax = data.defenceMax();
        this.monsterStatus.clear();
        rollMonsterIntent();
    }

    // ── Accessors ─────────────────────────────────────────────

    public int  getTurnNumber()  { return turnNumber;  }
    public void setTurnNumber(int n) { turnNumber = n; }
    public boolean isPlayerTurn(){ return playerTurn;   }
    public void setPlayerTurn(boolean b) { playerTurn = b; }

    // ── Monster intent ────────────────────────────────────────

    public void rollMonsterIntent() {
        if (ThreadLocalRandom.current().nextFloat() < 0.6f) {
            intentType = "ATTACK";
            intentValue = ThreadLocalRandom.current().nextInt(monsterAtkMin, monsterAtkMax + 1);
        } else {
            intentType = "DEFEND";
            intentValue = ThreadLocalRandom.current().nextInt(monsterDefMin, monsterDefMax + 1);
        }
    }

    // ── Win condition ─────────────────────────────────────────

    public Optional<GameOverEvent> checkWinCondition() {
        if (monsterHp <= 0) return Optional.of(new GameOverEvent(0));
        if (playerHp  <= 0) return Optional.of(new GameOverEvent(1));
        return Optional.empty();
    }

    // ── Event queue ───────────────────────────────────────────

    public void pushEvent(GameEvent e)        { eventQueue.addLast(e);  }
    public void pushEvents(List<GameEvent> e) { eventQueue.addAll(e);   }
    public GameEvent pollEvent()              { return eventQueue.pollFirst(); }
    public boolean hasEvents()                { return !eventQueue.isEmpty();  }
}
