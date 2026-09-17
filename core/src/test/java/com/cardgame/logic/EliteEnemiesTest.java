package com.cardgame.logic;

import com.cardgame.data.StatusEffect;
import com.cardgame.logic.monsters.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EliteEnemiesTest {
    private GameState state;

    @BeforeEach
    void setUp() {
        state = new GameState();
        state.initPlayer(80, 80, 3, new ArrayList<>());
    }

    @Test
    void testEliteDifficultyBetweenCommonAndBoss() {
        CrawlingEye eye = new CrawlingEye(0, 0);
        ChainedPatient chained = new ChainedPatient(0, 0);
        FrenziedPatient frenzied = new FrenziedPatient(0, 0);
        Boss boss = new Boss(0, 0);

        MaskedPatient masked = new MaskedPatient(0, 0);
        HeadNurse nurse = new HeadNurse(0, 0);

        // Both are marked as elite and not boss
        assertTrue(masked.isElite());
        assertFalse(masked.isBoss());
        assertTrue(nurse.isElite());
        assertFalse(nurse.isBoss());

        // HP is higher than common enemies
        assertTrue(masked.maxHp > chained.maxHp);
        assertTrue(masked.maxHp > frenzied.maxHp);
        assertTrue(masked.maxHp > eye.maxHp);

        assertTrue(nurse.maxHp > chained.maxHp);
        assertTrue(nurse.maxHp > frenzied.maxHp);
        assertTrue(nurse.maxHp > eye.maxHp);

        // HP is lower than the boss
        assertTrue(masked.maxHp < boss.maxHp);
        assertTrue(nurse.maxHp < boss.maxHp);
    }

    @Test
    void testMaskedPatientAttackAndFrenzyProgression() {
        MaskedPatient masked = new MaskedPatient(1000f, 280f);
        state.initMonsters(new MonsterGroup(masked));

        // Turn 1: Thrash (13 damage)
        assertEquals("ATTACK", masked.intentType);
        assertEquals(13, masked.intentValue);

        masked.takeTurn(state);
        assertEquals(80 - 13, state.playerHp);

        // Turn 2: Frenzy (Buff: +2 Strength, +8 Block)
        assertEquals("BUFF", masked.intentType);
        masked.takeTurn(state);
        assertEquals(2, masked.status.get(StatusEffect.STRENGTH));
        assertEquals(8, masked.block);

        // Turn 3: Flail (Multi-hit: 2 hits of 7 + 2 Strength = 9 * 2 = 18 damage)
        assertEquals("ATTACK", masked.intentType);
        assertEquals(18, masked.intentValue);
        int hpBefore = state.playerHp;
        masked.takeTurn(state);
        assertEquals(hpBefore - 18, state.playerHp);
    }

    @Test
    void testHeadNurseAppliesWeakAndPerformsLethalDose() {
        HeadNurse nurse = new HeadNurse(1000f, 280f);
        state.initMonsters(new MonsterGroup(nurse));

        // Turn 1: Sedative Injection (9 damage + 2 Weak)
        assertEquals("ATTACK_DEBUFF", nurse.intentType);
        assertEquals(9, nurse.intentValue);
        assertFalse(state.playerStatus.has(StatusEffect.WEAK));

        nurse.takeTurn(state);
        assertEquals(80 - 9, state.playerHp);
        assertTrue(state.playerStatus.has(StatusEffect.WEAK));
        assertEquals(2, state.playerStatus.get(StatusEffect.WEAK));

        // Turn 2: Stabilize (7 damage + 10 block + 1 strength)
        assertEquals("ATTACK_DEFEND", nurse.intentType);
        int hpBefore = state.playerHp;
        nurse.takeTurn(state);
        assertEquals(hpBefore - 7, state.playerHp);
        assertEquals(10, nurse.block);
        assertEquals(1, nurse.status.get(StatusEffect.STRENGTH));

        // Turn 3: Lethal Dose (15 + 1 Strength = 16 damage)
        assertEquals("ATTACK", nurse.intentType);
        assertEquals(16, nurse.intentValue);
        hpBefore = state.playerHp;
        nurse.takeTurn(state);
        assertEquals(hpBefore - 16, state.playerHp);
    }

    @Test
    void testEliteEnemiesCanNeverAppearInPairs() {
        MaskedPatient masked = new MaskedPatient(850f, 280f);
        HeadNurse nurse = new HeadNurse(1150f, 280f);
        ChainedPatient common = new ChainedPatient(1150f, 280f);

        // Attempting to create group with two elites
        MonsterGroup elitePair = new MonsterGroup(masked, nurse);
        assertEquals(1, elitePair.monsters.size(), "Two elites in a pair must be collapsed to exactly 1");
        assertEquals(1000f, elitePair.monsters.get(0).drawX);
        assertEquals(280f, elitePair.monsters.get(0).drawY);

        // Attempting to create group with elite and common enemy
        MonsterGroup eliteAndCommon = new MonsterGroup(nurse, common);
        assertEquals(1, eliteAndCommon.monsters.size(), "Elite with common monster must be collapsed to exactly 1");
        assertTrue(eliteAndCommon.monsters.get(0).isElite());
        assertEquals(1000f, eliteAndCommon.monsters.get(0).drawX);

        // Common enemies CAN still appear in pairs
        MonsterGroup commonPair = new MonsterGroup(new ChainedPatient(850f, 280f), new FrenziedPatient(1150f, 280f));
        assertEquals(2, commonPair.monsters.size(), "Common enemies can still appear in pairs");
    }
}
