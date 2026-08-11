package com.cardgame;
import com.cardgame.logic.cards.AbstractCard;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.cardgame.data.*;
import com.cardgame.logic.relics.*;
import com.cardgame.logic.potions.*;

import java.util.*;

/**
 * Root ApplicationListener.
 * Loads all game data from JSON and bootstraps the first screen.
 */
public class CardBattlerGame extends Game {

    private List<AbstractCard> allCards = Arrays.asList(
        new com.cardgame.logic.cards.FranticStrikeCard(),
        new com.cardgame.logic.cards.DenyRealityCard(),
        new com.cardgame.logic.cards.SkullCrackCard(),
        new com.cardgame.logic.cards.DelusionalStrikeCard(),
        new com.cardgame.logic.cards.DescentIntoMadnessCard(),
        new com.cardgame.logic.cards.HysteriaCard(),
        new com.cardgame.logic.cards.IntrusiveThoughtCard(),
        new com.cardgame.logic.cards.LashOutCard(),
        new com.cardgame.logic.cards.ManicBurstCard(),
        new com.cardgame.logic.cards.ManifestTraumaCard(),
        new com.cardgame.logic.cards.ObsessiveStrikeCard(),
        new com.cardgame.logic.cards.PsychoticBreakCard(),
        new com.cardgame.logic.cards.RecklessAbandonCard(),
        new com.cardgame.logic.cards.RepressCard(),
        new com.cardgame.logic.cards.SeethingHatredCard(),
        new com.cardgame.logic.cards.SharpenMindCard(),
        new com.cardgame.logic.cards.SplitPersonalityCard(),
        new com.cardgame.logic.cards.SteelNerveCard(),
        new com.cardgame.logic.cards.StubbornDenialCard(),
        new com.cardgame.logic.cards.SuddenPanicCard(),
        new com.cardgame.logic.cards.TraumatizeCard(),
        new com.cardgame.logic.cards.ViciousStrikeCard(),
        new com.cardgame.logic.cards.WildFlailingCard()
    );
    private List<CharacterData> allCharacters = Collections.emptyList();
    private List<MonsterData>   allMonsters   = Collections.emptyList();
    
    

    @Override
    public void create() {
        try {
            
            Gdx.app.log("Game", "Loaded " + allCards.size() + " cards.");
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load cards.json", e);
        }
        try {
            allCharacters = com.cardgame.data.CardDataLoader.loadCharacters(Gdx.files.internal("data/characters.json").read());
            Gdx.app.log("Game", "Loaded " + allCharacters.size() + " characters.");
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load characters.json", e);
        }
        try {
            allMonsters = com.cardgame.data.CardDataLoader.loadMonsters(Gdx.files.internal("data/monsters.json").read());
            Gdx.app.log("Game", "Loaded " + allMonsters.size() + " monsters.");
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load monsters.json", e);
        }
        try {
            
            
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load relics.json", e);
        }
        try {
            
            
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load potions.json", e);
        }

        setScreen(new com.cardgame.screens.MainMenuScreen(this));
    }

    public List<AbstractCard>      getAllCards()      { return allCards;      }
    public List<CharacterData> getAllCharacters() { return allCharacters; }
    public List<MonsterData>   getAllMonsters()   { return allMonsters;   }
    public List<AbstractRelic> getAllRelics() {
        return java.util.Arrays.asList(new TaintedIVBag(), new RustedScalpel(), new RorschachInkblot());
    }
    public List<AbstractPotion> getAllPotions() {
        return java.util.Arrays.asList(new AdrenalineSyringe(), new VialOfAcid(), new SteroidAmpoule());
    }

    /** Get cards for a specific character. */
    public List<AbstractCard> getCardsForCharacter(String charName) {
        List<AbstractCard> filtered = new ArrayList<>();
        for (AbstractCard c : allCards) {
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
