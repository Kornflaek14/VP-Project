package com.cardgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.cardgame.data.*;

import java.util.*;

/**
 * Root ApplicationListener.
 * Loads all game data from JSON and bootstraps the first screen.
 */
public class CardBattlerGame extends Game {

    private List<CardData>      allCards      = Collections.emptyList();
    private List<CharacterData> allCharacters = Collections.emptyList();
    private List<MonsterData>   allMonsters   = Collections.emptyList();
    private List<RelicData>     allRelics     = Collections.emptyList();
    private List<PotionData>    allPotions    = Collections.emptyList();

    @Override
    public void create() {
        try {
            allCards = CardDataLoader.loadCards(Gdx.files.internal("data/cards.json").read());
            Gdx.app.log("Game", "Loaded " + allCards.size() + " cards.");
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load cards.json", e);
        }
        try {
            allCharacters = CardDataLoader.loadCharacters(Gdx.files.internal("data/characters.json").read());
            Gdx.app.log("Game", "Loaded " + allCharacters.size() + " characters.");
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load characters.json", e);
        }
        try {
            allMonsters = CardDataLoader.loadMonsters(Gdx.files.internal("data/monsters.json").read());
            Gdx.app.log("Game", "Loaded " + allMonsters.size() + " monsters.");
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load monsters.json", e);
        }
        try {
            allRelics = CardDataLoader.loadRelics(Gdx.files.internal("data/relics.json").read());
            Gdx.app.log("Game", "Loaded " + allRelics.size() + " relics.");
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load relics.json", e);
        }
        try {
            allPotions = CardDataLoader.loadPotions(Gdx.files.internal("data/potions.json").read());
            Gdx.app.log("Game", "Loaded " + allPotions.size() + " potions.");
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load potions.json", e);
        }

        startNewGame();
    }

    public void startNewGame() {
        CharacterData ironclad = null;
        for (CharacterData c : allCharacters) {
            if (c.name().equalsIgnoreCase("Ironclad")) {
                ironclad = c;
                break;
            }
        }
        if (ironclad == null && !allCharacters.isEmpty()) ironclad = allCharacters.get(0);
        
        if (ironclad != null) {
            com.cardgame.logic.RunManager.getInstance().startNewRun(ironclad, getCardsForCharacter(ironclad.name()));
        }
        setScreen(new com.cardgame.screens.MapScreen(this));
    }

    public List<CardData>      getAllCards()      { return allCards;      }
    public List<CharacterData> getAllCharacters() { return allCharacters; }
    public List<MonsterData>   getAllMonsters()   { return allMonsters;   }
    public List<RelicData>     getAllRelics()     { return allRelics;     }
    public List<PotionData>    getAllPotions()    { return allPotions;    }

    /** Get cards for a specific character. */
    public List<CardData> getCardsForCharacter(String charName) {
        List<CardData> filtered = new ArrayList<>();
        for (CardData c : allCards) {
            if (c.character().equalsIgnoreCase(charName)) filtered.add(c);
        }
        return filtered;
    }

    /** Get monsters for a given level tier. */
    public List<MonsterData> getMonstersForLevel(int level) {
        List<MonsterData> filtered = new ArrayList<>();
        for (MonsterData m : allMonsters) {
            if (m.level() == level) filtered.add(m);
        }
        return filtered;
    }
}
