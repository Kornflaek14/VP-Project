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
            assertEquals(4, cards.size(), "Expected 4 cards in cards.json");
        }
    }

    @Test
    void load_goblinGrunt_hasCorrectStats() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/cards.json")) {
            Map<String, CardData> map = CardDataLoader.loadAsMap(is);
            CardData goblin = map.get("goblin_grunt");

            assertNotNull(goblin, "goblin_grunt not found");
            assertEquals("Goblin Grunt", goblin.name());
            assertEquals(1, goblin.manaCost());
            assertEquals(1, goblin.attack());
            assertEquals(2, goblin.health());
            assertTrue(goblin.abilityIds().isEmpty());
        }
    }

    @Test
    void load_stoneGolem_hasTauntAbility() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/cards.json")) {
            Map<String, CardData> map = CardDataLoader.loadAsMap(is);
            CardData golem = map.get("stone_golem");

            assertNotNull(golem);
            assertTrue(golem.abilityIds().contains("taunt"),
                    "stone_golem should have the 'taunt' ability ID");
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
