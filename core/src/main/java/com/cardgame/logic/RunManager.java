package com.cardgame.logic;
import com.cardgame.logic.rooms.*;
import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.logic.relics.*;
import com.cardgame.logic.potions.*;

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
    private final List<AbstractCard> deck = new ArrayList<>();
    private final List<AbstractRelic> relics = new ArrayList<>();
    private final List<AbstractPotion> potions = new ArrayList<>();

    // Map tracking
    private int currentNodeIndex = 0;
    private int maxNodes = 15;

    // ── Persistent map data ───────────────────────────────────
    private final List<MapNodeData> mapNodes = new ArrayList<>();
    private final List<Integer> pathTaken = new ArrayList<>();
    private int lastVisitedNodeId = -1;  // -1 means no node visited yet (start of run)

    /**
     * Simple data holder for a map node, persisted across screen transitions.
     */
    public static class MapNodeData {
        public final int id;
        public final int level;
        public String type;  // "COMBAT", "ELITE", "REST", "TREASURE", "SHOP", "BOSS"
        public AbstractRoom room;
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
    
    public List<Integer> getPathTaken() { return pathTaken; }

    public void startNewRun(CharacterData character, List<AbstractCard> characterCards) {
        this.selectedCharacter = character;
        this.maxHp = character.hp();
        this.currentHp = character.hp();
        this.gold = character.gold();
        this.deck.clear();
        this.deck.addAll(characterCards);
        this.relics.clear();
        addRelic(new TaintedIVBag());
        this.potions.clear();
        this.currentNodeIndex = 0;
        this.lastVisitedNodeId = -1;
        this.pathTaken.clear();
        this.mapNodes.clear();
        generateMap();
    }

    // ── Map generation ────────────────────────────────────────

    private class Edge {
        int srcCol, destCol;
        Edge(int s, int d) { srcCol = s; destCol = d; }
    }

    private void generateMap() {
        mapNodes.clear();
        Random rand = new Random();
        int maxLevels = 15;
        this.maxNodes = maxLevels;
        int maxCols = 7;
        float startY = 200f;
        float endY = 3800f;
        float spacingY = (endY - startY) / (maxLevels - 1);
        float viewportWidth = 1440f; 
        float spacingX = viewportWidth / (maxCols + 1f);

        List<List<Edge>> levelEdges = new ArrayList<>();
        for (int i = 0; i < maxLevels - 1; i++) levelEdges.add(new ArrayList<>());
        
        // Generate 6 paths from floor 1 (level 1) up to the floor before the boss (level maxLevels - 2)
        for (int p = 0; p < 6; p++) {
            int currentCol = rand.nextInt(maxCols);
            for (int l = 1; l < maxLevels - 2; l++) {
                int nextCol = currentCol;
                int move = rand.nextInt(3) - 1; // -1, 0, or 1
                
                boolean valid = false;
                int attempts = 0;
                while (!valid && attempts < 10) {
                    nextCol = currentCol + move;
                    valid = true;
                    if (nextCol < 0 || nextCol >= maxCols) {
                        valid = false;
                    } else {
                        // Check crossing
                        for (Edge e : levelEdges.get(l)) {
                            if ((currentCol < e.srcCol && nextCol > e.destCol) || 
                                (currentCol > e.srcCol && nextCol < e.destCol)) {
                                valid = false;
                                break;
                            }
                        }
                    }
                    if (!valid) move = rand.nextInt(3) - 1;
                    attempts++;
                }
                
                if (!valid) nextCol = currentCol; // straight line fallback
                
                boolean exists = false;
                for (Edge e : levelEdges.get(l)) {
                    if (e.srcCol == currentCol && e.destCol == nextCol) { exists = true; break; }
                }
                if (!exists) {
                    levelEdges.get(l).add(new Edge(currentCol, nextCol));
                }
                
                currentCol = nextCol;
            }
        }
        
        MapNodeData[][] grid = new MapNodeData[maxLevels][maxCols];
        int idCounter = 0;
        
        // 1) Create the Single Start Node (Level 0)
        grid[0][3] = new MapNodeData(idCounter++, 0, "COMBAT", spacingX * 4, startY);
        mapNodes.add(grid[0][3]);

        // 2) Create the Single Boss Node (Level maxLevels - 1)
        grid[maxLevels-1][3] = new MapNodeData(idCounter++, maxLevels-1, "BOSS", spacingX * 4, startY + (maxLevels-1) * spacingY);
        mapNodes.add(grid[maxLevels-1][3]);

        // 3) Create all intermediary nodes based on generated paths
        for (int l = 1; l < maxLevels - 2; l++) {
            for (Edge e : levelEdges.get(l)) {
                if (grid[l][e.srcCol] == null) {
                    float jx = (rand.nextFloat() - 0.5f) * (spacingX * 0.4f);
                    float jy = (rand.nextFloat() - 0.5f) * (spacingY * 0.4f);
                    grid[l][e.srcCol] = new MapNodeData(idCounter++, l, "UNKNOWN", spacingX * (e.srcCol + 1) + jx, startY + l * spacingY + jy);
                    mapNodes.add(grid[l][e.srcCol]);
                }
                if (grid[l+1][e.destCol] == null) {
                    float jx = (rand.nextFloat() - 0.5f) * (spacingX * 0.4f);
                    float jy = (rand.nextFloat() - 0.5f) * (spacingY * 0.4f);
                    grid[l+1][e.destCol] = new MapNodeData(idCounter++, l+1, "UNKNOWN", spacingX * (e.destCol + 1) + jx, startY + (l+1) * spacingY + jy);
                    mapNodes.add(grid[l+1][e.destCol]);
                }
                if (!grid[l][e.srcCol].nextNodeIds.contains(grid[l+1][e.destCol].id)) {
                    grid[l][e.srcCol].nextNodeIds.add(grid[l+1][e.destCol].id);
                }
            }
        }
        
        // 4) Hook up the Start Node to all generated nodes on Level 1
        for (int c = 0; c < maxCols; c++) {
            if (grid[1][c] != null) {
                grid[0][3].nextNodeIds.add(grid[1][c].id);
            }
        }

        // 5) Hook up all generated nodes on Level maxLevels - 2 to the Boss Node
        for (int c = 0; c < maxCols; c++) {
            if (grid[maxLevels-2][c] != null) {
                grid[maxLevels-2][c].nextNodeIds.add(grid[maxLevels-1][3].id);
            }
        }
        
        for (MapNodeData node : mapNodes) {
            assignNodeType(node, maxLevels, grid, rand);
        }
        
        // Fix duplicate outgoing types
        for (MapNodeData node : mapNodes) {
            List<Integer> outIds = node.nextNodeIds;
            if (outIds.size() > 1) {
                for (int i = 0; i < outIds.size(); i++) {
                    for (int j = i + 1; j < outIds.size(); j++) {
                        MapNodeData d1 = getNodeById(outIds.get(i));
                        MapNodeData d2 = getNodeById(outIds.get(j));
                        if (d1 != null && d2 != null && d1.type.equals(d2.type)) {
                            int tries = 0;
                            while(d2.type.equals(d1.type) && tries < 10) {
                                mutateTypeSafely(d2, maxLevels, grid, rand);
                                tries++;
                            }
                        }
                    }
                }
            }
        }
        
        // Finalize rooms
        for (MapNodeData node : mapNodes) {
             switch(node.type) {
                case "COMBAT": node.room = new MonsterRoom(); break;
                case "ELITE": node.room = new EliteRoom(); break;
                case "BOSS": node.room = new BossRoom(); break;
                case "REST": node.room = new RestRoom(); break;
                case "SHOP": node.room = new ShopRoom(); break;
                case "TREASURE": node.room = new TreasureRoom(); break;
                default: node.room = new MonsterRoom();
            }
        }
    }

    private void assignNodeType(MapNodeData node, int maxLevels, MapNodeData[][] grid, Random rand) {
        int l = node.level;
        if (l == 0) { node.type = "COMBAT"; return; }
        if (l == 8) { node.type = "TREASURE"; return; }
        if (l == maxLevels - 1) { node.type = "BOSS"; return; } 
        if (l == maxLevels - 2) { node.type = "REST"; return; }
        
        List<String> pool = new ArrayList<>();
        pool.add("COMBAT");
        pool.add("COMBAT");
        pool.add("COMBAT");
        
        boolean parentIsElite = false;
        boolean parentIsShop = false;
        boolean parentIsRest = false;
        
        for (MapNodeData[] row : grid) {
            for (MapNodeData parent : row) {
                if (parent != null && parent.nextNodeIds.contains(node.id)) {
                    if ("ELITE".equals(parent.type)) parentIsElite = true;
                    if ("SHOP".equals(parent.type)) parentIsShop = true;
                    if ("REST".equals(parent.type)) parentIsRest = true;
                }
            }
        }
        
        if (l >= 5 && !parentIsElite) {
            pool.add("ELITE");
            pool.add("ELITE");
        }
        if (l != maxLevels - 3 && !parentIsRest) { 
            pool.add("REST");
        }
        if (!parentIsShop) {
            pool.add("SHOP");
        }
        
        node.type = pool.get(rand.nextInt(pool.size()));
    }
    
    private void mutateTypeSafely(MapNodeData node, int maxLevels, MapNodeData[][] grid, Random rand) {
        int l = node.level;
        if (l == 0 || l == 8 || l == maxLevels - 2 || l == maxLevels - 1) return; 
        assignNodeType(node, maxLevels, grid, rand); 
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
    public List<AbstractCard> getDeck() { return deck; }
    public void addCardToDeck(AbstractCard card) { deck.add(card); }
    public void removeCardFromDeck(AbstractCard card) { deck.remove(card); }

    // ── Relics ────────────────────────────────────────────────
    public List<AbstractRelic> getRelics() { return relics; }
    public void addRelic(AbstractRelic relic) { relics.add(relic); relic.onEquip(); }

    /** Sum of all relic attack boosts. */
    public int getTotalAttackBoost() { return 0; }

    /** Sum of all relic defence boosts. */
    public int getTotalDefenceBoost() { return 0; }

    /** Sum of all relic energy boosts. */
    public int getTotalEnergyBoost() { return 0; }

    // ── Potions ───────────────────────────────────────────────
    public List<AbstractPotion> getPotions() { return potions; }
    public boolean addPotion(AbstractPotion potion) {
        if (potions.size() < 3) { potions.add(potion); return true; }
        return false;
    }
        public void usePotion(int index, GameState state) {
        if (index >= 0 && index < potions.size()) {
            AbstractPotion p = potions.remove(index);
            com.cardgame.logic.monsters.AbstractMonster target = null;
            if (p.isTargeted() && state != null && state.monsterGroup != null) {
                for(com.cardgame.logic.monsters.AbstractMonster m : state.monsterGroup.monsters) {
                    if (m.currentHp > 0) { target = m; break; }
                }
            }
            p.use(state, target);
        }
    }


    // ── Map progress ──────────────────────────────────────────
    public int getCurrentNodeIndex() { return currentNodeIndex; }
    public void advanceNode() { currentNodeIndex++; }
    public int getMaxNodes() { return maxNodes; }
}
