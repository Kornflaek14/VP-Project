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
 *
 * HARD RULE: no libGDX imports.
 */
public final class CombatResolver {

    /**
     * Resolves the combat phase for the active player.
     */
    public List<GameEvent> resolveCombatPhase(GameState state, int attackingPlayerIndex) {
        List<GameEvent> events = new ArrayList<>();
        CardInstance[] board = state.getBoard(attackingPlayerIndex);
        int defendingPlayer = 1 - attackingPlayerIndex;
        CardInstance[] defenderBoard = state.getBoard(defendingPlayer);

        // Pre-compute taunt slot: if the defending board has any live taunt unit,
        // every attacker that would hit an empty slot or a non-taunt unit must
        // redirect to the first taunt unit's slot instead.
        int tauntSlot = -1;
        for (int i = 0; i < defenderBoard.length; i++) {
            CardInstance d = defenderBoard[i];
            if (d != null && !d.isDead() && d.hasTaunt()) {
                tauntSlot = i;
                break;
            }
        }

        for (int i = 0; i < board.length; i++) {
            CardInstance attacker = board[i];
            if (attacker == null || attacker.isDead()) continue;

            // Determine targets based on sigils
            List<Integer> targetSlots = getAttackTargets(attacker, state, i);

            for (int targetSlot : targetSlots) {
                if (targetSlot < 0 || targetSlot >= com.cardgame.utils.Constants.MAX_BOARD_SIZE) continue;

                // Enforce taunt: redirect to taunt slot if a taunt unit exists and
                // the attacker cannot bypass it (Airborne bypasses via canAttackDirectly).
                if (tauntSlot != -1 && targetSlot != tauntSlot) {
                    CardInstance intended = defenderBoard[targetSlot];
                    boolean canBypass = false;
                    for (String id : attacker.getTemplate().abilityIds()) {
                        com.cardgame.logic.abilities.Ability a =
                                AbilityRegistry.getInstance().get(id).orElse(null);
                        if (a != null && a.canAttackDirectly(attacker, intended)) {
                            canBypass = true;
                            break;
                        }
                    }
                    if (!canBypass) {
                        targetSlot = tauntSlot;
                    }
                }

                events.addAll(resolveAttack(attacker, targetSlot, state, attackingPlayerIndex));
            }
        }
        return events;
    }

    private List<GameEvent> resolveAttack(CardInstance attacker, int targetSlot, GameState state, int attackingPlayer) {
        List<GameEvent> events = new ArrayList<>();
        int defendingPlayer = 1 - attackingPlayer;
        CardInstance defender = state.getBoard(defendingPlayer)[targetSlot];

        // Check Airborne / direct-hit bypass
        boolean canAttackDirectly = false;
        for (String id : attacker.getTemplate().abilityIds()) {
            com.cardgame.logic.abilities.Ability a = AbilityRegistry.getInstance().get(id).orElse(null);
            if (a != null && a.canAttackDirectly(attacker, defender)) {
                canAttackDirectly = true;
            }
        }

        int damage = attacker.getEffectiveAttack();
        if (damage <= 0) return events;

        if (defender == null || canAttackDirectly) {
            // ── Direct scale hit ────────────────────────────────────────────────
            if (attackingPlayer == 0) {
                state.setScaleBalance(state.getScaleBalance() + damage);
            } else {
                state.setScaleBalance(state.getScaleBalance() - damage);
            }
            events.add(new ScaleChangedEvent(state.getScaleBalance(), damage, attackingPlayer));
            // Win condition: scale tips ±WINNING_SCALE_THRESHOLD
            state.checkWinCondition().ifPresent(events::add);
        } else {
            // ── Minion vs minion ────────────────────────────────────────────────
            events.add(new CardAttackedEvent(attacker, defender));

            // 1. Affinity multiplier on attacker → defender
            float attackMult = AffinityResolver.getMultiplier(
                    attacker.getTemplate().affinityType(),
                    defender.getTemplate().affinityType());
            int finalDamage = (int) (damage * attackMult);

            if (finalDamage > 0) {
                defender.dealDamage(finalDamage);
                events.add(new DamageDealtEvent(attacker, defender, finalDamage, true));
                events.addAll(processDeath(defender, state));
            }

            // 2. Melee retaliation: defender strikes back unless attacker is Ranged
            //    (attacker.isRangedThisTurn() is set by RangedAbility.onAttack which
            //    fires below — so we must check the template's ability list here instead)
            boolean isRanged = attacker.getTemplate().abilityIds().stream()
                    .anyMatch(id -> id.equals("ranged"));
            if (!isRanged && !attacker.isDead() && !defender.isDead()) {
                int retaliationDamage = defender.getEffectiveAttack();
                if (retaliationDamage > 0) {
                    float retMult = AffinityResolver.getMultiplier(
                            defender.getTemplate().affinityType(),
                            attacker.getTemplate().affinityType());
                    int finalRetaliation = (int) (retaliationDamage * retMult);
                    if (finalRetaliation > 0) {
                        attacker.dealDamage(finalRetaliation);
                        events.add(new DamageDealtEvent(defender, attacker, finalRetaliation, true));
                        events.addAll(processDeath(attacker, state));
                    }
                }
            }

            // 3. Thorns: if defender has thorns and attacker is alive (melee only)
            if (!isRanged && !attacker.isDead() && defender.getThornsValue() > 0) {
                int thornsDmg = defender.getThornsValue();
                attacker.dealDamage(thornsDmg);
                events.add(new DamageDealtEvent(defender, attacker, thornsDmg, false));
                events.addAll(processDeath(attacker, state));
            }

            // 4. Trigger onAttack abilities (e.g. FreezeOnHit, RangedAbility flag)
            for (String id : attacker.getTemplate().abilityIds()) {
                AbilityRegistry.getInstance()
                               .get(id)
                               .ifPresent(a -> events.addAll(a.onAttack(attacker, defender, state)));
            }
        }
        return events;
    }

    private List<Integer> getAttackTargets(CardInstance attacker, GameState state, int attackerSlot) {
        for (String id : attacker.getTemplate().abilityIds()) {
            com.cardgame.logic.abilities.Ability a = AbilityRegistry.getInstance().get(id).orElse(null);
            if (a != null) {
                List<Integer> override = a.getAttackTargets(attacker, state, attackerSlot);
                if (override != null && !override.isEmpty()) {
                    return override;
                }
            }
        }
        return List.of(attackerSlot); // default
    }

    private List<GameEvent> processDeath(CardInstance card, GameState state) {
        List<GameEvent> events = new ArrayList<>();
        if (!card.isDead()) return events;

        int ownerIdx = state.findBoardOwner(card);
        if (ownerIdx == -1) return events;

        state.removeCardFromBoard(card);
        state.setBones(ownerIdx, state.getBones(ownerIdx) + 1); // Grant bone
        events.add(new CardDiedEvent(ownerIdx, card));

        for (String id : card.getTemplate().abilityIds()) {
            AbilityRegistry.getInstance()
                           .get(id)
                           .ifPresent(a -> events.addAll(a.onDeath(card, state)));
        }
        return events;
    }
}
