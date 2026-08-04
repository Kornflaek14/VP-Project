package com.cardgame.logic;

import com.cardgame.data.CardData;
import com.cardgame.logic.abilities.AbilityRegistry;
import com.cardgame.logic.actions.PlayCardAction;
import com.cardgame.logic.events.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PlayCardAction}.
 * <p>
 * No libGDX graphics context is required — these run in a plain JVM.
 */
class PlayCardActionTest {

    private GameState state;
    private CardData  goblinData;
    private CardInstance goblin;

    @BeforeAll
    static void setUpRegistry() {
        // Ensure ability registry is populated for tests involving abilities
        AbilityRegistry.getInstance().registerDefaults();
    }

    @BeforeEach
    void setUp() {
        state = new GameState();
        goblinData = new CardData("goblin_grunt", "Goblin Grunt", 1, 2, 0, 
                com.cardgame.data.CardType.UNIT, com.cardgame.data.UnitArchetype.STANDARD, com.cardgame.data.AffinityType.NEUTRAL, 
                "", List.of(), List.of(), "A scrappy fighter.", 1);
        goblin = new CardInstance(goblinData, 0);
        state.getPlayer(0).hand.add(goblin);
        state.getPlayer(0).maxEnergy = 3;
        state.getPlayer(0).currentEnergy = 3;
    }

    // ── Happy-path tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Playing a card removes it from the player's hand")
    void playCard_removesFromHand() {
        new PlayCardAction(0, goblin, -1, null).execute(state);
        assertFalse(state.getPlayer(0).hand.contains(goblin));
    }

    @Test
    @DisplayName("Playing a card adds it to the player's board")
    void playCard_addsToBoard() {
        new PlayCardAction(0, goblin, -1, null).execute(state);
        assertTrue(java.util.Arrays.asList(state.getPlayer(0).board).contains(goblin));
    }

    @Test
    @DisplayName("Playing a card deducts its energy cost")
    void playCard_deductsEnergy() {
        new PlayCardAction(0, goblin, -1, null).execute(state);
        assertTrue(java.util.Arrays.asList(state.getPlayer(0).board).contains(goblin));
    }

    @Test
    @DisplayName("execute() returns a CardPlayedEvent")
    void playCard_returnsCardPlayedEvent() {
        List<GameEvent> events = new PlayCardAction(0, goblin, -1, null).execute(state);
        assertTrue(events.stream().anyMatch(e -> e instanceof CardPlayedEvent),
                "Expected at least one CardPlayedEvent");
    }

    @Test
    @DisplayName("Card is placed at the specified board position")
    void playCard_respectsBoardPosition() {
        // Put a second card on the board first
        CardInstance other = new CardInstance(goblinData, 0);
        state.getPlayer(0).board[1] = other;

        // Play goblin at position 0 (leftmost)
        new PlayCardAction(0, goblin, 0, null).execute(state);

        assertEquals(goblin, state.getPlayer(0).board[0]);
    }

    @Test
    @DisplayName("Playing a Taunt card sets its taunt flag")
    void playCard_tauntAbility_setsTauntFlag() {
        CardData tauntCard = new CardData("stone_golem", "Stone Golem", 1, 7, 0,
                com.cardgame.data.CardType.UNIT, com.cardgame.data.UnitArchetype.STANDARD, com.cardgame.data.AffinityType.NEUTRAL, 
                "", List.of("taunt"), List.of(), "Taunt.", 1);
        CardInstance golem = new CardInstance(tauntCard, 0);
        state.getPlayer(0).hand.add(golem);

        new PlayCardAction(0, golem, -1, null).execute(state);

        assertTrue(golem.hasTaunt(), "Taunt ability should have set the taunt flag");
    }

    @Test
    @DisplayName("Playing a Charge card clears its exhausted flag")
    void playCard_chargeAbility_clearsExhaustedFlag() {
        CardData chargeCard = new CardData("fire_imp", "Fire Imp", 3, 1, 0,
                com.cardgame.data.CardType.UNIT, com.cardgame.data.UnitArchetype.STANDARD, com.cardgame.data.AffinityType.NEUTRAL, 
                "", List.of("charge"), List.of(), "Charge.", 1);
        CardInstance imp = new CardInstance(chargeCard, 0);
        state.getPlayer(0).hand.add(imp);

        new PlayCardAction(0, imp, -1, null).execute(state);

        assertFalse(imp.isExhausted(), "Charge ability should have cleared summoning sickness");
    }

    // ── Error-path tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Playing with insufficient energy throws IllegalStateException")
    void playCard_insufficientEnergy_throws() {
        CardData expensive = new CardData("expensive", "Expensive", 0, 5, 5,
                com.cardgame.data.CardType.UNIT, com.cardgame.data.UnitArchetype.STANDARD, com.cardgame.data.AffinityType.NEUTRAL, 
                "", List.of(), List.of(), "", 1);
        CardInstance expCard = new CardInstance(expensive, 0);
        state.getPlayer(0).hand.add(expCard);
        
        state.getPlayer(0).currentEnergy = 0;

        assertThrows(IllegalStateException.class,
                () -> new PlayCardAction(0, expCard, -1, null).execute(state));
    }

    @Test
    @DisplayName("Playing a card not in hand throws IllegalStateException")
    void playCard_cardNotInHand_throws() {
        state.getPlayer(0).hand.clear();

        assertThrows(IllegalStateException.class,
                () -> new PlayCardAction(0, goblin, -1, null).execute(state));
    }

    @Test
    @DisplayName("Playing onto a full board throws IllegalStateException")
    void playCard_fullBoard_throws() {
        // Fill the board to maximum capacity
        for (int i = 0; i < com.cardgame.utils.Constants.MAX_BOARD_SIZE; i++) {
            state.getPlayer(0).board[i] = new CardInstance(goblinData, 0);
        }

        assertThrows(IllegalStateException.class,
                () -> new PlayCardAction(0, goblin, -1, null).execute(state));
    }
}
