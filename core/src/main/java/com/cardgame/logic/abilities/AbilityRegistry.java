package com.cardgame.logic.abilities;

import com.cardgame.logic.abilities.impl.ChargeAbility;
import com.cardgame.logic.abilities.impl.DeathrattleDrawAbility;
import com.cardgame.logic.abilities.impl.TauntAbility;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Maps JSON ability ID strings (e.g. {@code "taunt"}) to {@link Ability} implementations.
 *
 * <pre>
 * // Bootstrap once (e.g. in CardBattlerGame.create()):
 * AbilityRegistry.getInstance().registerDefaults();
 *
 * // Retrieve at runtime:
 * Optional&lt;Ability&gt; ability = AbilityRegistry.getInstance().get("taunt");
 * </pre>
 */
public final class AbilityRegistry {

    private static final AbilityRegistry INSTANCE = new AbilityRegistry();

    private final Map<String, Ability> registry = new HashMap<>();

    private AbilityRegistry() {}

    public static AbilityRegistry getInstance() {
        return INSTANCE;
    }

    // ── Registration ───────────────────────────────────────────────────────────

    /** Registers a single ability implementation under the given JSON id. */
    public void register(String id, Ability ability) {
        registry.put(id, ability);
    }

    /**
     * Registers all built-in abilities.
     * Call this once during application startup, before any game logic runs.
     */
    public void registerDefaults() {
        register("taunt",           new TauntAbility());
        register("charge",          new ChargeAbility());
        register("deathrattle_draw", new DeathrattleDrawAbility());
    }

    // ── Lookup ─────────────────────────────────────────────────────────────────

    /** Returns the ability for the given id, or {@link Optional#empty()} if unknown. */
    public Optional<Ability> get(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    /** Read-only view of the full registry (useful for tooling / tests). */
    public Map<String, Ability> asMap() {
        return Collections.unmodifiableMap(registry);
    }
}
