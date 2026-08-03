package com.cardgame.logic;

import com.cardgame.data.StatusEffectData;
import com.cardgame.data.StatusEffectType;
import com.cardgame.logic.events.StatusEffectAppliedEvent;
import com.cardgame.logic.events.StatusEffectExpiredEvent;
import com.cardgame.logic.events.DamageDealtEvent;

import java.util.Iterator;
import java.util.List;

public final class StatusEffectProcessor {

    private StatusEffectProcessor() {}

    /**
     * Applies a new effect to a card and emits an event.
     */
    public static void applyEffect(GameState state, CardInstance target, StatusEffectData effect) {
        target.getActiveEffects().add(new ActiveEffect(effect.type(), effect.value(), effect.duration()));
        
        if (effect.type() == StatusEffectType.FREEZE) {
            target.setFrozen(true);
        } else if (effect.type() == StatusEffectType.THORNS) {
            target.setThornsValue(target.getThornsValue() + effect.value());
        }
        
        state.pushEvent(new StatusEffectAppliedEvent(target, effect.type(), effect.value(), effect.duration()));
    }

    /**
     * Processes start-of-turn effects for a player's board (e.g. poison ticks).
     */
    public static void processTurnStart(GameState state, int playerIndex) {
        CardInstance[] board = state.getBoard(playerIndex);
        
        for (CardInstance card : board) {
            if (card == null || card.isDead()) continue;
            
            Iterator<ActiveEffect> iter = card.getActiveEffects().iterator();
            while (iter.hasNext()) {
                ActiveEffect effect = iter.next();
                
                // Trigger effects
                if (effect.getType() == StatusEffectType.POISON) {
                    card.dealDamage(effect.getValue());
                    state.pushEvent(new DamageDealtEvent(null, card, effect.getValue(), false));
                } else if (effect.getType() == StatusEffectType.HEAL_AURA) {
                    // Heal all friendly units (board is sparse; guard against null slots)
                    for (CardInstance friendly : board) {
                        if (friendly != null && !friendly.isDead() && friendly != card) {
                            friendly.heal(effect.getValue());
                            // Could push heal event
                        }
                    }
                }
                
                // Decrement duration
                effect.decrementDuration();
                
                // Expiry
                if (effect.isExpired()) {
                    if (effect.getType() == StatusEffectType.FREEZE) {
                        card.setFrozen(false);
                    } else if (effect.getType() == StatusEffectType.THORNS) {
                        card.setThornsValue(Math.max(0, card.getThornsValue() - effect.getValue()));
                    }
                    state.pushEvent(new StatusEffectExpiredEvent(card, effect.getType()));
                    iter.remove();
                }
            }
        }
    }
}
