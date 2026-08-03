package com.cardgame.logic.events;

/**
 * Sealed marker interface for every game event that the UI (BattleScreen) reacts to.
 * <p>
 * All permitted subtypes are plain records — no libGDX imports allowed anywhere
 * in this package.
 */
public sealed interface GameEvent
        permits CardPlayedEvent,
                CardAttackedEvent,
                CardDiedEvent,
                DamageDealtEvent,
                ScaleChangedEvent,
                CardDrawnEvent,
                TurnChangedEvent,
                GameOverEvent,
                StatusEffectAppliedEvent,
                StatusEffectExpiredEvent,
                TokenSpawnedEvent,
                SacrificeEvent {
}
