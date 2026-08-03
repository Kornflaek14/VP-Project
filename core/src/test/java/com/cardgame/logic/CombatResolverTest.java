package com.cardgame.logic;

import com.cardgame.data.CardData;
import com.cardgame.logic.abilities.AbilityRegistry;
import com.cardgame.logic.events.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CombatResolver}.
 * <p>
 * No libGDX graphics context — plain JVM.
 */
class CombatResolverTest {

    private GameState      state;
    private CombatResolver resolver;

    @BeforeAll
    static void setUpRegistry() {
        AbilityRegistry.getInstance().registerDefaults();
    }

    @BeforeEach
    void setUp() {
        state    = new GameState();
        resolver = new CombatResolver();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private CardInstance minion(String name, int atk, int hp, int owner, List<String> abilities) {
        CardData d = new CardData("id_" + name, name, 0, atk, hp, abilities, "");
        CardInstance ci = new CardInstance(d, owner);
        ci.setExhausted(false);
        state.getPlayer(owner).board.add(ci);
        return ci;
    }

    private CardInstance minion(String name, int atk, int hp, int owner) {
        return minion(name, atk, hp, owner, List.of());
    }

    // ── Tests ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Attacker deals damage equal to its attack to the defender")
    void resolve_dealsDamageToDefender() {
        CardInstance atk = minion("Attacker", 3, 5, 0);
        CardInstance def = minion("Defender", 1, 4, 1);

        resolver.resolve(atk, def, state);

        assertEquals(1, def.getCurrentHealth(), "4 HP − 3 damage = 1 HP");
    }

    @Test
    @DisplayName("Defender deals damage equal to its attack back to the attacker")
    void resolve_dealsDamageToAttacker() {
        CardInstance atk = minion("Attacker", 2, 3, 0);
        CardInstance def = minion("Defender", 2, 5, 1);

        resolver.resolve(atk, def, state);

        assertEquals(1, atk.getCurrentHealth(), "3 HP − 2 damage = 1 HP");
    }

    @Test
    @DisplayName("A minion with 0 HP is marked dead")
    void resolve_minionDies_isMarkedDead() {
        CardInstance atk = minion("Attacker", 5, 5, 0);
        CardInstance def = minion("Defender", 1, 2, 1);

        resolver.resolve(atk, def, state);

        assertTrue(def.isDead(), "Defender should be dead after taking 5 damage to 2 HP");
    }

    @Test
    @DisplayName("Dead minion is removed from its owner's board")
    void resolve_deadMinion_removedFromBoard() {
        CardInstance atk = minion("Attacker", 5, 5, 0);
        CardInstance def = minion("Defender", 1, 2, 1);

        resolver.resolve(atk, def, state);

        assertFalse(state.getPlayer(1).board.contains(def),
                "Dead defender should be removed from player 1's board");
    }

    @Test
    @DisplayName("resolve() emits a CardDiedEvent when a minion dies")
    void resolve_deadMinion_emitsCardDiedEvent() {
        CardInstance atk = minion("Attacker", 5, 5, 0);
        CardInstance def = minion("Defender", 1, 2, 1);

        List<GameEvent> events = resolver.resolve(atk, def, state);

        assertTrue(events.stream().anyMatch(e -> e instanceof CardDiedEvent cde && cde.card() == def),
                "Expected a CardDiedEvent for the dead defender");
    }

    @Test
    @DisplayName("resolve() emits DamageDealtEvent for each minion that takes damage")
    void resolve_emitsDamageDealtEvents() {
        CardInstance atk = minion("Attacker", 3, 5, 0);
        CardInstance def = minion("Defender", 2, 4, 1);

        List<GameEvent> events = resolver.resolve(atk, def, state);

        long damageEvents = events.stream().filter(e -> e instanceof DamageDealtEvent).count();
        assertEquals(2, damageEvents, "Two DamageDealtEvents expected (one per combatant)");
    }

    @Test
    @DisplayName("Both minions can die in the same combat exchange")
    void resolve_tradeKills_bothDie() {
        CardInstance atk = minion("Attacker", 5, 2, 0);
        CardInstance def = minion("Defender", 5, 2, 1);

        List<GameEvent> events = resolver.resolve(atk, def, state);

        assertTrue(atk.isDead(), "Attacker should be dead");
        assertTrue(def.isDead(), "Defender should be dead");
        assertFalse(state.getPlayer(0).board.contains(atk));
        assertFalse(state.getPlayer(1).board.contains(def));

        long deathEvents = events.stream().filter(e -> e instanceof CardDiedEvent).count();
        assertEquals(2, deathEvents, "Two CardDiedEvents expected");
    }

    @Test
    @DisplayName("Deathrattle: Draw draws a card for the owner on death")
    void resolve_deathrattleDraw_drawsCard() {
        // Give player 1 a deck with one card
        CardData deckCard = new CardData("deck_c", "Deck Card", 1, 1, 1, List.of(), "");
        state.getPlayer(1).deck.add(deckCard);

        CardInstance atk = minion("Attacker", 5, 5, 0);
        CardInstance def = minion("Deathrattler", 1, 1, 1, List.of("deathrattle_draw"));

        List<GameEvent> events = resolver.resolve(atk, def, state);

        assertTrue(events.stream().anyMatch(e -> e instanceof CardDrawnEvent cde && cde.playerIndex() == 1),
                "Expected a CardDrawnEvent for player 1 from deathrattle");
        assertEquals(1, state.getPlayer(1).hand.size(), "Player 1 should have drawn a card");
    }
}
