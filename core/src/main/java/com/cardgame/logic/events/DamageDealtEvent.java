package com.cardgame.logic.events;

import com.cardgame.logic.monsters.AbstractMonster;

public record DamageDealtEvent(String source, String target, int amount, AbstractMonster targetMonster) implements GameEvent {
    public DamageDealtEvent(String source, String target, int amount) {
        this(source, target, amount, null);
    }
}
