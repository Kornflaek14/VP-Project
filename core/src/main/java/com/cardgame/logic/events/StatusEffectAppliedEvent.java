package com.cardgame.logic.events;

import com.cardgame.data.StatusEffect;

/** Fired when a status effect is applied to a combatant. */
public final class StatusEffectAppliedEvent implements GameEvent {
    private final String target; // "player" or "monster"
    private final StatusEffect effect;
    private final int stacks;

    public StatusEffectAppliedEvent(String target, StatusEffect effect, int stacks) {
        this.target = target;
        this.effect = effect;
        this.stacks  = stacks;
    }

    public String target()        { return target; }
    public StatusEffect effect()  { return effect; }
    public int stacks()           { return stacks; }
}
