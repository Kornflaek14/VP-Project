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
        CardData d = new CardData("id_" + name, name, atk, hp, 0, 0,
                com.cardgame.data.CardType.UNIT, com.cardgame.data.UnitArchetype.STANDARD, com.cardgame.data.AffinityType.NEUTRAL, 
                "", abilities, List.of(), "");
        CardInstance ci = new CardInstance(d, owner);
        ci.setExhausted(false);
        for (int i = 0; i < state.getPlayer(owner).board.length; i++) {
            if (state.getPlayer(owner).board[i] == null) {
                state.getPlayer(owner).board[i] = ci;
                break;
            }
        }
        return ci;
    }

    private CardInstance minion(String name, int atk, int hp, int owner) {
        return minion(name, atk, hp, owner, List.of());
    }

    // ── Tests ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Attacker deals damage equal to its attack to the defender")
    void resolveCombatPhase_dealsDamageToDefender() {
        CardInstance atk = minion("Attacker", 3, 5, 0);
        CardInstance def = minion("Defender", 1, 4, 1);

        resolver.resolveCombatPhase(state, 0);

        assertEquals(1, def.getCurrentHealth(), "4 HP − 3 damage = 1 HP");
    }

    @Test
    @DisplayName("Attacker does not receive retaliation damage")
    void resolveCombatPhase_noRetaliation() {
        CardInstance atk = minion("Attacker", 2, 3, 0);
        CardInstance def = minion("Defender", 2, 5, 1);

        resolver.resolveCombatPhase(state, 0);

        assertEquals(3, atk.getCurrentHealth(), "Attacker should not take damage in its own combat phase");
    }

    @Test
    @DisplayName("A minion with 0 HP is marked dead and gives a bone")
    void resolveCombatPhase_minionDies_isMarkedDeadAndGivesBone() {
        CardInstance atk = minion("Attacker", 5, 5, 0);
        CardInstance def = minion("Defender", 1, 2, 1);
        
        state.setBones(1, 0);

        resolver.resolveCombatPhase(state, 0);

        assertTrue(def.isDead(), "Defender should be dead after taking 5 damage to 2 HP");
        assertEquals(1, state.getBones(1), "Player 1 should get a bone when their minion dies");
    }

    @Test
    @DisplayName("Dead minion is removed from its owner's board")
    void resolveCombatPhase_deadMinion_removedFromBoard() {
        CardInstance atk = minion("Attacker", 5, 5, 0);
        CardInstance def = minion("Defender", 1, 2, 1);

        resolver.resolveCombatPhase(state, 0);

        assertFalse(java.util.Arrays.asList(state.getPlayer(1).board).contains(def),
                "Dead defender should be removed from player 1's board");
    }

    @Test
    @DisplayName("Direct attack damages the scale")
    void resolveCombatPhase_directAttack_damagesScale() {
        CardInstance atk = minion("Attacker", 3, 5, 0);
        // no defender

        resolver.resolveCombatPhase(state, 0);

        assertEquals(3, state.getScaleBalance(), "Player 0 deals 3 damage to the scale");
    }

    @Test
    @DisplayName("Deathrattle: Draw draws a card for the owner on death")
    void resolveCombatPhase_deathrattleDraw_drawsCard() {
        // Give player 1 a deck with one card
        CardData deckCard = new CardData("deck_c", "Deck Card", 1, 1, 1, 1,
                com.cardgame.data.CardType.UNIT, com.cardgame.data.UnitArchetype.STANDARD, com.cardgame.data.AffinityType.NEUTRAL, 
                "", List.of(), List.of(), "");
        state.getPlayer(1).deck.add(deckCard);

        CardInstance atk = minion("Attacker", 5, 5, 0);
        CardInstance def = minion("Deathrattler", 1, 1, 1, List.of("deathrattle_draw"));

        List<GameEvent> events = resolver.resolveCombatPhase(state, 0);

        assertTrue(events.stream().anyMatch(e -> e instanceof CardDrawnEvent cde && cde.playerIndex() == 1),
                "Expected a CardDrawnEvent for player 1 from deathrattle");
        assertEquals(1, state.getPlayer(1).hand.size(), "Player 1 should have drawn a card");
    }
}
