package com.cardgame.data;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that {@link CardDataLoader} correctly parses {@code cards.json}
 * from the test classpath (sourced from {@code assets/}).
 * <p>
 * No libGDX context required — plain JVM.
 */
class CardDataLoaderTest {

    @Test
    void load_parsesAllFourCards() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/cards.json")) {
            assertNotNull(is, "cards.json not found on test classpath");
            List<CardData> cards = CardDataLoader.load(is);
            assertEquals(7, cards.size(), "Expected 7 card templates in cards.json");
        }
    }

    @Test
    void load_scalpel_hasCorrectStats() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/cards.json")) {
            Map<String, CardData> map = CardDataLoader.loadAsMap(is);
            CardData scalpel = map.get("scalpel");

            assertNotNull(scalpel, "scalpel not found");
            assertEquals("Scalpel", scalpel.name());
            assertEquals(1, scalpel.bloodCost());
            assertEquals(2, scalpel.attack());
            assertEquals(1, scalpel.health());
            assertTrue(scalpel.abilityIds().isEmpty());
        }
    }

    @Test
    void load_mask_hasTauntAbility() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/cards.json")) {
            Map<String, CardData> map = CardDataLoader.loadAsMap(is);
            CardData brute = map.get("mask");

            assertNotNull(brute);
            assertTrue(brute.abilityIds().contains("taunt"),
                    "mask should have the 'taunt' ability ID");
        }
    }

    @Test
    void load_allCardsHaveNonEmptyIds() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/cards.json")) {
            List<CardData> cards = CardDataLoader.load(is);
            for (CardData card : cards) {
                assertFalse(card.id().isBlank(), "Card ID must not be blank: " + card);
                assertFalse(card.name().isBlank(), "Card name must not be blank: " + card);
            }
        }
    }
}
