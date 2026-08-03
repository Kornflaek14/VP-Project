package com.cardgame.logic.actions;

import com.cardgame.logic.CardInstance;
import com.cardgame.logic.CombatResolver;
import com.cardgame.logic.GameState;
import com.cardgame.logic.events.GameEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Orders one minion to attack another minion (or, in future, a hero).
 *
 * <pre>
 * new AttackAction(attacker, defender).execute(state);
 * </pre>
 *
 * The action validates that the attacker is not exhausted, delegates damage
 * resolution to {@link CombatResolver}, then marks the attacker as exhausted.
 */
public final class AttackAction implements GameAction {

    private final CardInstance     attacker;
    private final CardInstance     defender;
    private final CombatResolver   resolver;

    public AttackAction(CardInstance attacker, CardInstance defender) {
        this(attacker, defender, new CombatResolver());
    }

    /** Constructor for injecting a custom resolver (e.g. in tests). */
    public AttackAction(CardInstance attacker, CardInstance defender, CombatResolver resolver) {
        this.attacker = attacker;
        this.defender = defender;
        this.resolver = resolver;
    }

    @Override
    public List<GameEvent> execute(GameState state) {
        List<GameEvent> events = new ArrayList<>();

        // ── Validate ──────────────────────────────────────────────────────────
        if (attacker.isExhausted()) {
            throw new IllegalStateException(
                    "'" + attacker.getTemplate().name() + "' is exhausted and cannot attack this turn.");
        }
        if (attacker.isDead()) {
            throw new IllegalStateException("Cannot attack with a dead minion.");
        }
        if (defender.isDead()) {
            throw new IllegalStateException("Cannot attack a dead minion.");
        }

        // ── Resolve combat (modifies both cards' health, removes dead cards) ──
        events.addAll(resolver.resolve(attacker, defender, state));

        // ── Exhaust attacker (if it survived) ─────────────────────────────────
        if (!attacker.isDead()) {
            attacker.setExhausted(true);
        }

        return events;
    }
}
