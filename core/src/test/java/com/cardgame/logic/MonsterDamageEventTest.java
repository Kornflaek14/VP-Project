package com.cardgame.logic;

import com.cardgame.logic.events.GameEvent;
import com.cardgame.logic.events.PlayerDamagedEvent;
import com.cardgame.logic.monsters.FrenziedPatient;
import com.cardgame.logic.monsters.MonsterGroup;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonsterDamageEventTest {
    @Test
    void emitsOnlyActualHpLossAfterBlock() {
        GameState state = state();
        CombatResolver resolver = new CombatResolver();
        assertTrue(resolver.executeMonsterTurn(state).isEmpty()); // First turn is a buff.
        state.playerBlock = 4;
        List<GameEvent> events = resolver.executeMonsterTurn(state);
        assertEquals(75, state.playerHp);
        assertEquals(List.of(new PlayerDamagedEvent(5)), events);
    }

    @Test
    void fullyBlockedAttackDoesNotEmitHurt() {
        GameState state = state();
        CombatResolver resolver = new CombatResolver();
        resolver.executeMonsterTurn(state);
        state.playerBlock = 20;
        assertTrue(resolver.executeMonsterTurn(state).isEmpty());
        assertEquals(80, state.playerHp);
    }

    private GameState state() {
        GameState state = new GameState();
        state.playerHp = 80;
        state.monsterGroup = new MonsterGroup(new FrenziedPatient(1000f, 250f));
        return state;
    }
}
