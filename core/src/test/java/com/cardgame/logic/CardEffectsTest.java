package com.cardgame.logic;

import com.cardgame.logic.cards.FranticStrikeCard;
import com.cardgame.logic.cards.RepressCard;
import com.cardgame.logic.cards.SeethingHatredCard;
import com.cardgame.logic.cards.StubbornDenialCard;
import com.cardgame.logic.cards.ViciousStrikeCard;
import com.cardgame.logic.monsters.FrenziedPatient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardEffectsTest {

    private GameState state;

    @BeforeEach
    void setUp() {
        state = new GameState();
        state.playerHp = 80;
        state.playerMaxHp = 80;
        state.playerBlock = 0;
        state.playerEnergy = 3;
        RunManager.getInstance().setBaseAttackBonus(0);
        RunManager.getInstance().setBaseDefenseBonus(0);
    }

    @AfterEach
    void tearDown() {
        RunManager.getInstance().setBaseAttackBonus(0);
        RunManager.getInstance().setBaseDefenseBonus(0);
    }

    @Test
    void testBrickExhaustsRandomCardFromHand() {
        StubbornDenialCard brick = new StubbornDenialCard();
        FranticStrikeCard otherCard1 = new FranticStrikeCard();
        FranticStrikeCard otherCard2 = new FranticStrikeCard();

        state.hand.add(otherCard1);
        state.hand.add(otherCard2);

        assertEquals(2, state.hand.size());
        assertEquals(0, state.exhaustPile.size());

        brick.use(state, null);

        // One card exhausted from hand
        assertEquals(1, state.hand.size());
        assertEquals(1, state.exhaustPile.size());
        assertEquals(7, state.playerBlock);
    }

    @Test
    void testBrickWithEmptyHandDoesNotCrash() {
        StubbornDenialCard brick = new StubbornDenialCard();
        assertEquals(0, state.hand.size());

        brick.use(state, null);

        assertEquals(0, state.hand.size());
        assertEquals(0, state.exhaustPile.size());
        assertEquals(7, state.playerBlock);
    }

    @Test
    void testGlareAddsCopyToDiscardPile() {
        SeethingHatredCard glare = new SeethingHatredCard();
        FrenziedPatient target = new FrenziedPatient(1000f, 250f);
        target.currentHp = 30;

        assertEquals(0, state.discardPile.size());

        glare.use(state, target);

        assertEquals(24, target.currentHp); // 30 - 6
        assertEquals(1, state.discardPile.size());
        assertEquals("Glare", state.discardPile.get(0).name());
    }

    @Test
    void testPillsDrawsCardAndShufflesDiscardIfDrawEmpty() {
        RepressCard pills = new RepressCard();
        FranticStrikeCard cardInDiscard = new FranticStrikeCard();

        state.drawPile.clear();
        state.discardPile.add(cardInDiscard);
        state.hand.clear();

        pills.use(state, null);

        assertEquals(8, state.playerBlock);
        assertEquals(1, state.hand.size());
        assertEquals(cardInDiscard, state.hand.get(0));
        assertEquals(0, state.drawPile.size());
        assertEquals(0, state.discardPile.size());
    }

    @Test
    void testShardDealsDamageAndDrawsCard() {
        ViciousStrikeCard shard = new ViciousStrikeCard();
        FrenziedPatient target = new FrenziedPatient(1000f, 250f);
        target.currentHp = 30;

        FranticStrikeCard cardInDraw = new FranticStrikeCard();
        state.drawPile.add(cardInDraw);
        state.hand.clear();

        shard.use(state, target);

        assertEquals(21, target.currentHp); // 30 - 9
        assertEquals(1, state.hand.size());
        assertEquals(cardInDraw, state.hand.get(0));
        assertEquals(0, state.drawPile.size());
    }
}
