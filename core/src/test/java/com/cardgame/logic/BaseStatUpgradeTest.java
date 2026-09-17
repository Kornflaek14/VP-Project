package com.cardgame.logic;

import com.cardgame.logic.cards.DenyRealityCard;
import com.cardgame.logic.cards.FranticStrikeCard;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.monsters.FrenziedPatient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseStatUpgradeTest {

    @BeforeEach
    @AfterEach
    void resetBonuses() {
        RunManager.getInstance().setBaseAttackBonus(0);
        RunManager.getInstance().setBaseDefenseBonus(0);
    }

    @Test
    void testRunManagerBonusTracking() {
        RunManager rm = RunManager.getInstance();
        assertEquals(0, rm.getBaseAttackBonus());
        assertEquals(0, rm.getBaseDefenseBonus());
        assertEquals(0, rm.getTotalAttackBoost());
        assertEquals(0, rm.getTotalDefenceBoost());

        rm.addBaseAttackBonus(1);
        assertEquals(1, rm.getBaseAttackBonus());
        assertEquals(1, rm.getTotalAttackBoost());

        rm.addBaseAttackBonus(2);
        assertEquals(3, rm.getBaseAttackBonus());
        assertEquals(3, rm.getTotalAttackBoost());

        rm.addBaseDefenseBonus(1);
        assertEquals(1, rm.getBaseDefenseBonus());
        assertEquals(1, rm.getTotalDefenceBoost());
    }

    @Test
    void testBaseAttackIncreasesCardDamage() {
        RunManager rm = RunManager.getInstance();
        rm.setBaseAttackBonus(2);

        GameState state = new GameState();
        FrenziedPatient monster = new FrenziedPatient(1000f, 250f);
        monster.currentHp = 30;
        monster.maxHp = 30;
        monster.block = 0;

        FranticStrikeCard strike = new FranticStrikeCard(); // Base 7 dmg
        List<GameEvent> events = strike.use(state, monster);

        // Expected damage: 7 + 2 = 9. 30 - 9 = 21.
        assertEquals(21, monster.currentHp);
    }

    @Test
    void testBaseDefenseIncreasesCardBlock() {
        RunManager rm = RunManager.getInstance();
        rm.setBaseDefenseBonus(3);

        GameState state = new GameState();
        state.playerBlock = 0;

        DenyRealityCard card = new DenyRealityCard(); // Base 4 block
        List<GameEvent> events = card.use(state, null);

        // Expected block: 4 + 3 = 7.
        assertEquals(7, state.playerBlock);
    }
}
