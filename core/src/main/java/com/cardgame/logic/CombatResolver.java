package com.cardgame.logic;

import com.cardgame.logic.abilities.AbilityRegistry;
import com.cardgame.logic.events.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the outcome of a minion-vs-minion combat exchange.
 * <p>
 * <ul>
 *   <li>Both minions deal their attack damage simultaneously.</li>
 *   <li>Dead minions are removed from their owner's board.</li>
 *   <li>{@code onDeath} ability hooks are triggered after removal.</li>
 * </ul>
 *
 * The attacker is NOT exhausted here — that is the responsibility of the
 * caller ({@link com.cardgame.logic.actions.AttackAction}).
 * <p>
 * HARD RULE: no libGDX imports.
 */
public final class CombatResolver {

    /**
     * Executes one combat exchange between {@code attacker} and {@code defender}.
     *
     * @param attacker the attacking minion
     * @param defender the defending minion
     * @param state    current game state (boards are mutated when minions die)
     * @return ordered list of events produced by the exchange
     */
    public List<GameEvent> resolve(CardInstance attacker,
                                   CardInstance defender,
                                   GameState    state) {
        List<GameEvent> events = new ArrayList<>();

        // Announce the attack
        events.add(new CardAttackedEvent(attacker, defender));

        int attackerDamage = attacker.getTemplate().attack();
        int defenderDamage = defender.getTemplate().attack();

        // ── Apply damage simultaneously ────────────────────────────────────────
        if (attackerDamage > 0) {
            defender.dealDamage(attackerDamage);
            events.add(new DamageDealtEvent(defender, attackerDamage));
        }
        if (defenderDamage > 0) {
            attacker.dealDamage(defenderDamage);
            events.add(new DamageDealtEvent(attacker, defenderDamage));
        }

        // ── Trigger onAttack abilities ─────────────────────────────────────────
        for (String id : attacker.getTemplate().abilityIds()) {
            AbilityRegistry.getInstance()
                           .get(id)
                           .ifPresent(a -> events.addAll(a.onAttack(attacker, defender, state)));
        }

        // ── Check and process deaths ──────────────────────────────────────────
        // Defender checked first — symmetric order for attacker/defender ties.
        events.addAll(processDeath(defender, state));
        events.addAll(processDeath(attacker, state));

        return events;
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private List<GameEvent> processDeath(CardInstance card, GameState state) {
        List<GameEvent> events = new ArrayList<>();

        if (!card.isDead()) return events;

        // Find owning board and remove card
        int ownerIdx = state.findBoardOwner(card);
        if (ownerIdx == -1) return events; // already removed (shouldn't happen normally)

        state.getPlayer(ownerIdx).board.remove(card);
        events.add(new CardDiedEvent(ownerIdx, card));

        // Trigger onDeath abilities (deathrattles)
        for (String id : card.getTemplate().abilityIds()) {
            AbilityRegistry.getInstance()
                           .get(id)
                           .ifPresent(a -> events.addAll(a.onDeath(card, state)));
        }

        return events;
    }
}
