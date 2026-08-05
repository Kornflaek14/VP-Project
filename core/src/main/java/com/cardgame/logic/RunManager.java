package com.cardgame.logic;

import com.cardgame.data.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the persistent roguelike run state between battles.
 * Tracks: selected character, HP, gold, deck, relics, potions, floor progress.
 */
public class RunManager {

    private static RunManager instance;

    private CharacterData selectedCharacter;
    private int maxHp = 80;
    private int currentHp = 80;
    private int gold = 99;
    private final List<CardData> deck = new ArrayList<>();
    private final List<RelicData> relics = new ArrayList<>();
    private final List<PotionData> potions = new ArrayList<>();

    // Map tracking
    private int currentNodeIndex = 0;
    private int maxNodes = 15;

    private RunManager() {}

    public static RunManager getInstance() {
        if (instance == null) {
            instance = new RunManager();
        }
        return instance;
    }

    public void startNewRun(CharacterData character, List<CardData> characterCards) {
        this.selectedCharacter = character;
        this.maxHp = character.hp();
        this.currentHp = character.hp();
        this.gold = character.gold();
        this.deck.clear();
        this.deck.addAll(characterCards);
        this.relics.clear();
        this.potions.clear();
        this.currentNodeIndex = 0;
    }

    // ── Character ─────────────────────────────────────────────
    public CharacterData getSelectedCharacter() { return selectedCharacter; }

    // ── HP ────────────────────────────────────────────────────
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int hp) { this.currentHp = Math.min(hp, maxHp); }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int max) { this.maxHp = max; }
    public void heal(int amount) { this.currentHp = Math.min(currentHp + amount, maxHp); }

    // ── Gold ──────────────────────────────────────────────────
    public int getGold() { return gold; }
    public void addGold(int amount) { this.gold += amount; }
    public boolean spendGold(int amount) {
        if (gold >= amount) { gold -= amount; return true; }
        return false;
    }

    // ── Deck ──────────────────────────────────────────────────
    public List<CardData> getDeck() { return deck; }
    public void addCardToDeck(CardData card) { deck.add(card); }
    public void removeCardFromDeck(CardData card) { deck.remove(card); }

    // ── Relics ────────────────────────────────────────────────
    public List<RelicData> getRelics() { return relics; }
    public void addRelic(RelicData relic) { relics.add(relic); }

    /** Sum of all relic attack boosts. */
    public int getTotalAttackBoost() {
        int total = 0;
        for (RelicData r : relics) total += r.attackBoost();
        return total;
    }

    /** Sum of all relic defence boosts. */
    public int getTotalDefenceBoost() {
        int total = 0;
        for (RelicData r : relics) total += r.defenceBoost();
        return total;
    }

    /** Sum of all relic energy boosts. */
    public int getTotalEnergyBoost() {
        int total = 0;
        for (RelicData r : relics) total += r.energyBoost();
        return total;
    }

    // ── Potions ───────────────────────────────────────────────
    public List<PotionData> getPotions() { return potions; }
    public boolean addPotion(PotionData potion) {
        if (potions.size() < 3) { potions.add(potion); return true; }
        return false;
    }
    public void usePotion(int index) {
        if (index >= 0 && index < potions.size()) {
            PotionData p = potions.remove(index);
            currentHp = Math.min(currentHp + p.hpBoost(), maxHp);
        }
    }

    // ── Map progress ──────────────────────────────────────────
    public int getCurrentNodeIndex() { return currentNodeIndex; }
    public void advanceNode() { currentNodeIndex++; }
    public int getMaxNodes() { return maxNodes; }
}
