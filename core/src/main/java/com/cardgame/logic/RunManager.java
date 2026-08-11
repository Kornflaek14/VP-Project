package com.cardgame.logic;

import com.cardgame.data.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    // ── Persistent map data ───────────────────────────────────
    private final List<MapNodeData> mapNodes = new ArrayList<>();
    private int lastVisitedNodeId = -1;  // -1 means no node visited yet (start of run)

    /**
     * Simple data holder for a map node, persisted across screen transitions.
     */
    public static class MapNodeData {
        public final int id;
        public final int level;
        public final String type;  // "COMBAT", "ELITE", "REST", "TREASURE", "SHOP", "BOSS"
        public final float x;
        public final float y;
        public final List<Integer> nextNodeIds = new ArrayList<>();

        public MapNodeData(int id, int level, String type, float x, float y) {
            this.id = id;
            this.level = level;
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

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
        this.lastVisitedNodeId = -1;
        this.mapNodes.clear();
        generateMap();
    }

    // ── Map generation ────────────────────────────────────────

    private void generateMap() {
        mapNodes.clear();
        Random rand = new Random();
        int nodeIdCounter = 0;
        float startY = 100f;
        float endY = 900f - 100f; // Constants.VIEWPORT_HEIGHT - 100
        float spacingY = (endY - startY) / (maxNodes - 1);
        float viewportWidth = 1440f; // Constants.VIEWPORT_WIDTH

        List<MapNodeData> prevLevelNodes = new ArrayList<>();

        for (int l = 0; l < maxNodes; l++) {
            int nodesInLevel = (l == 0 || l == maxNodes - 1) ? 1 : 2 + rand.nextInt(3);
            float spacingX = viewportWidth / (nodesInLevel + 1f);

            List<MapNodeData> currentLevelNodes = new ArrayList<>();
            for (int i = 0; i < nodesInLevel; i++) {
                float x = spacingX * (i + 1);
                float y = startY + l * spacingY;

                String type = determineNodeType(l, maxNodes, rand);
                MapNodeData node = new MapNodeData(nodeIdCounter++, l, type, x, y);
                mapNodes.add(node);
                currentLevelNodes.add(node);
            }

            // Connect previous level to current level
            if (!prevLevelNodes.isEmpty()) {
                for (int i = 0; i < prevLevelNodes.size(); i++) {
                    MapNodeData prev = prevLevelNodes.get(i);
                    int targetIndex = (i * currentLevelNodes.size()) / prevLevelNodes.size();
                    if (!prev.nextNodeIds.contains(currentLevelNodes.get(targetIndex).id)) {
                        prev.nextNodeIds.add(currentLevelNodes.get(targetIndex).id);
                    }

                    if (rand.nextBoolean() && targetIndex + 1 < currentLevelNodes.size()) {
                        if (!prev.nextNodeIds.contains(currentLevelNodes.get(targetIndex + 1).id)) {
                            prev.nextNodeIds.add(currentLevelNodes.get(targetIndex + 1).id);
                        }
                    }
                }
                // Ensure every node in current level has at least one incoming edge
                for (int i = 0; i < currentLevelNodes.size(); i++) {
                    MapNodeData curr = currentLevelNodes.get(i);
                    boolean hasIncoming = false;
                    for (MapNodeData prev : prevLevelNodes) {
                        if (prev.nextNodeIds.contains(curr.id)) {
                            hasIncoming = true;
                            break;
                        }
                    }
                    if (!hasIncoming) {
                        MapNodeData randomPrev = prevLevelNodes.get(rand.nextInt(prevLevelNodes.size()));
                        if (!randomPrev.nextNodeIds.contains(curr.id)) {
                            randomPrev.nextNodeIds.add(curr.id);
                        }
                    }
                }
            }
            prevLevelNodes = currentLevelNodes;
        }
    }

    private String determineNodeType(int level, int maxLevels, Random rand) {
        if (level == maxLevels - 1) return "BOSS";
        if (level == 0) return "COMBAT";

        int roll = rand.nextInt(100);
        if (roll < 40) return "COMBAT";
        if (roll < 60) return "ELITE";
        if (roll < 75) return "REST";
        if (roll < 90) return "SHOP";
        return "TREASURE";
    }

    // ── Map accessors ─────────────────────────────────────────

    public List<MapNodeData> getMapNodes() { return mapNodes; }

    public int getLastVisitedNodeId() { return lastVisitedNodeId; }

    public void setLastVisitedNodeId(int id) { this.lastVisitedNodeId = id; }

    public MapNodeData getNodeById(int id) {
        for (MapNodeData n : mapNodes) {
            if (n.id == id) return n;
        }
        return null;
    }

    /**
     * Returns the set of node IDs that are reachable from the last visited node.
     * If no node visited yet, returns all level-0 nodes.
     */
    public List<Integer> getReachableNodeIds() {
        List<Integer> reachable = new ArrayList<>();
        if (lastVisitedNodeId == -1) {
            // Start of run: all level-0 nodes are reachable
            for (MapNodeData n : mapNodes) {
                if (n.level == 0) reachable.add(n.id);
            }
        } else {
            MapNodeData lastNode = getNodeById(lastVisitedNodeId);
            if (lastNode != null) {
                reachable.addAll(lastNode.nextNodeIds);
            }
        }
        return reachable;
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
