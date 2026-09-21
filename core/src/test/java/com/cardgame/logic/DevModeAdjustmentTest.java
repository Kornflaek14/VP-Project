package com.cardgame.logic;

import com.cardgame.data.CharacterData;
import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.logic.cards.FranticStrikeCard;
import com.cardgame.logic.monsters.HeadNurse;
import com.cardgame.logic.monsters.MaskedPatient;
import com.cardgame.logic.monsters.MonsterGroup;
import com.cardgame.logic.potions.AdrenalineSyringe;
import com.cardgame.logic.potions.ManaPotion;
import com.cardgame.logic.potions.SteroidAmpoule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DevModeAdjustmentTest {

    private RunManager run;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        run = RunManager.getInstance();
        List<AbstractCard> starter = new ArrayList<>();
        starter.add(new FranticStrikeCard());
        run.startNewRun(new CharacterData("The Patient", "", "", "", 3, 80, 100, "", ""), starter);

        gameState = new GameState();
        gameState.initPlayer(run.getCurrentHp(), run.getMaxHp(), 3, run.getDeck());
    }

    @Test
    void testRestoreHealthInRunManagerAndGameState() {
        // Simulate taking damage in combat
        gameState.playerHp = 25;
        run.setCurrentHp(25);
        assertEquals(25, gameState.playerHp);
        assertEquals(25, run.getCurrentHp());

        // Apply Restore Health adjustment
        run.setCurrentHp(run.getMaxHp());
        gameState.playerHp = gameState.playerMaxHp;

        assertEquals(80, run.getCurrentHp());
        assertEquals(80, gameState.playerHp);
        assertEquals(run.getMaxHp(), gameState.playerHp);
    }

    @Test
    void testRaiseMaxHealthIncreasesCurrentAndMaxHp() {
        int initialMax = run.getMaxHp();
        int initialCurrent = run.getCurrentHp();

        run.setMaxHp(initialMax + 20);
        run.setCurrentHp(initialCurrent + 20);
        gameState.playerMaxHp = run.getMaxHp();
        gameState.playerHp = Math.min(gameState.playerHp + 20, gameState.playerMaxHp);

        assertEquals(initialMax + 20, run.getMaxHp());
        assertEquals(initialCurrent + 20, run.getCurrentHp());
        assertEquals(100, gameState.playerMaxHp);
        assertEquals(100, gameState.playerHp);
    }

    @Test
    void testRefillEnergyIncreasesPlayerEnergy() {
        gameState.playerEnergy = 0;
        assertEquals(0, gameState.playerEnergy);

        gameState.playerEnergy = Math.max(gameState.playerEnergy, gameState.playerMaxEnergy) + 3;
        assertTrue(gameState.playerEnergy >= 6);
    }

    @Test
    void testDefeatEncounterSetsMonsterHpToZero() {
        MaskedPatient masked = new MaskedPatient(0, 0);
        HeadNurse nurse = new HeadNurse(0, 0);
        MonsterGroup group = new MonsterGroup(masked, nurse);
        gameState.initMonsters(group);

        assertFalse(group.areMonstersBasicallyDead());

        // Simulate killing all monsters via developer cheat
        for (var m : gameState.monsterGroup.monsters) {
            m.currentHp = 0;
        }

        assertTrue(group.areMonstersBasicallyDead());
        assertTrue(gameState.checkWinCondition().isPresent());
        assertEquals(0, gameState.checkWinCondition().get().winnerIndex());
    }

    @Test
    void testDrawCheatCardsAddsToHand() {
        gameState.drawPile.add(new FranticStrikeCard());
        gameState.drawPile.add(new FranticStrikeCard());
        gameState.drawPile.add(new FranticStrikeCard());

        int handBefore = gameState.hand.size();
        gameState.drawCards(3);

        assertEquals(handBefore + 3, gameState.hand.size());
    }

    @Test
    void testRefillPotions() {
        run.getPotions().clear();
        assertTrue(run.getPotions().isEmpty());

        run.addPotion(new AdrenalineSyringe());
        run.addPotion(new ManaPotion());
        run.addPotion(new SteroidAmpoule());

        assertEquals(3, run.getPotions().size());
    }

    @Test
    void testEliteEnemySpriteFilesExist() {
        String[] eliteSpriteFiles = {
            "assets/Character sprite/Enemies/Elite enemy/nurse/idle/Idle.png",
            "assets/Character sprite/Enemies/Elite enemy/nurse/attack/attack1.png",
            "assets/Character sprite/Enemies/Elite enemy/nurse/attack/attack2.png",
            "assets/Character sprite/Enemies/Elite enemy/nurse/attack/attack3.png",
            "assets/Character sprite/Enemies/Elite enemy/nurse/attack/attack4.png",
            "assets/Character sprite/Enemies/Elite enemy/masked/idle/Idle.png",
            "assets/Character sprite/Enemies/Elite enemy/masked/attack/attack1.png",
            "assets/Character sprite/Enemies/Elite enemy/masked/attack/attack2.png",
            "assets/Character sprite/Enemies/Elite enemy/masked/attack/attack3.png",
            "assets/Character sprite/Enemies/Elite enemy/masked/attack/attack4.png"
        };

        for (String path : eliteSpriteFiles) {
            File file = new File(path);
            if (!file.exists()) {
                file = new File("../" + path);
            }
            assertTrue(file.exists(), "Elite enemy sprite file must exist: " + path);
        }
    }
}
