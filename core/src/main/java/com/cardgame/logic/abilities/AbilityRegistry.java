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
        register("deathrattle_draw",new DeathrattleDrawAbility());
        
        // New thematic abilities
        register("thorns_2", new com.cardgame.logic.abilities.impl.ThornsAbility(2));
        register("freeze_on_hit_1", new com.cardgame.logic.abilities.impl.FreezeOnHitAbility(1));
        register("heal_hero_2", new com.cardgame.logic.abilities.impl.HealHeroAbility(2));
        register("heal_hero_5", new com.cardgame.logic.abilities.impl.HealHeroAbility(5));
        register("aoe_buff_atk_1", new com.cardgame.logic.abilities.impl.AoeBuffAtkAbility(1));
        register("spell_damage_3", new com.cardgame.logic.abilities.impl.SpellDamageAbility(3));
        register("spell_damage_5", new com.cardgame.logic.abilities.impl.SpellDamageAbility(5));
        register("targeted_freeze_2", new com.cardgame.logic.abilities.impl.TargetedFreezeAbility(2));
        register("area_freeze_1", new com.cardgame.logic.abilities.impl.AreaFreezeAbility(1));
        register("area_poison_1_3", new com.cardgame.logic.abilities.impl.AreaPoisonAbility(1, 3));
        register("airborne", new com.cardgame.logic.abilities.impl.AirborneAbility());
        register("mighty_leap", new com.cardgame.logic.abilities.impl.MightyLeapAbility());
        register("bifurcated_strike", new com.cardgame.logic.abilities.impl.BifurcatedStrikeAbility());
        register("ranged", new com.cardgame.logic.abilities.impl.RangedAbility());
        register("give_blood_3", new com.cardgame.logic.abilities.impl.GiveBloodAbility(3));
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
