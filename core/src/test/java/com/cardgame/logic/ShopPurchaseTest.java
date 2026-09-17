package com.cardgame.logic;

import com.cardgame.data.CharacterData;
import com.cardgame.data.StatusEffect;
import com.cardgame.logic.cards.FranticStrikeCard;
import com.cardgame.logic.potions.AdrenalineSyringe;
import com.cardgame.logic.potions.ManaPotion;
import com.cardgame.logic.potions.SteroidAmpoule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopPurchaseTest {
    private final RunManager run = RunManager.getInstance();

    @BeforeEach void startRun() {
        run.startNewRun(new CharacterData("The Patient", "", "", "", 3, 80, 99, "", ""), List.of());
    }

    @Test void fullPotionBeltDoesNotChargeGold() {
        for (int i = 0; i < 3; i++) run.addPotion(new ManaPotion());
        assertFalse(run.buyPotion(new AdrenalineSyringe(), 40));
        assertEquals(99, run.getGold());
        assertEquals(3, run.getPotions().size());
    }

    @Test void insufficientFundsLeaveBothInventoriesUntouched() {
        assertFalse(run.buyPotion(new ManaPotion(), 100));
        assertFalse(run.buyCard(new FranticStrikeCard(), 100));
        assertEquals(99, run.getGold());
        assertTrue(run.getDeck().isEmpty());
        assertTrue(run.getPotions().isEmpty());
    }

    @Test void purchaseMakesIndependentInventoryCopiesAndChargesOnce() {
        FranticStrikeCard card = new FranticStrikeCard();
        ManaPotion potion = new ManaPotion();
        assertTrue(run.buyCard(card, 45));
        assertTrue(run.buyPotion(potion, 40));
        assertEquals(14, run.getGold());
        assertNotSame(card, run.getDeck().get(0));
        assertNotSame(potion, run.getPotions().get(0));
        run.getDeck().get(0).upgrade();
        assertFalse(card.isUpgraded());
    }

    @Test void invalidPriceCannotCreateGold() {
        assertFalse(run.buyCard(new FranticStrikeCard(), -1));
        assertFalse(run.buyPotion(new ManaPotion(), -1));
        assertFalse(run.spendGold(-1));
        assertEquals(99, run.getGold());
    }

    @Test void threeRemediesApplyTheirAdvertisedCombatEffectsAndAreConsumed() {
        GameState state = new GameState();
        state.playerBlock = 3;
        state.playerEnergy = 1;
        state.playerMaxEnergy = 3;
        run.addPotion(new AdrenalineSyringe());
        run.addPotion(new SteroidAmpoule());
        run.addPotion(new ManaPotion());
        run.usePotion(0, state);
        assertEquals(15, state.playerBlock);
        run.usePotion(0, state);
        assertEquals(2, state.playerStatus.get(StatusEffect.STRENGTH));
        run.usePotion(0, state);
        assertEquals(3, state.playerEnergy);
        assertEquals(3, state.playerMaxEnergy);
        assertTrue(run.getPotions().isEmpty());
    }
}
