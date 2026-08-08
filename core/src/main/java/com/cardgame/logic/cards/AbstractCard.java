package com.cardgame.logic.cards;

import com.cardgame.data.CardType;
import com.cardgame.logic.GameState;
import com.cardgame.logic.RunManager;
import com.cardgame.data.StatusEffect;
import com.cardgame.logic.events.*;

import java.util.List;
import java.util.UUID;

public abstract class AbstractCard {
    public final String uuid = UUID.randomUUID().toString();
    public String id;
    public String name;
    public int energyCost;
    public String character;
    public int baseDamage;
    public int baseBlock;
    public String description;
    public String image;
    public CardType cardType;
    public boolean upgraded = false;

    public AbstractCard(String id, String name, int energyCost, String character,
                        int damage, int block, String description, String image, CardType cardType) {
        this.id = id;
        this.name = name;
        this.energyCost = energyCost;
        this.character = character;
        this.baseDamage = damage;
        this.baseBlock = block;
        this.description = description;
        this.image = image;
        this.cardType = cardType;
    }

    public String id() { return id; }
    public String name() { return name; }
    public int energyCost() { return energyCost; }
    public String character() { return character; }
    public int damage() { return baseDamage; }
    public int defence() { return baseBlock; }
    public String description() { return description; }
    public String image() { return image; }
    public CardType cardType() { return cardType; }
    public boolean isUpgraded() { return upgraded; }

    
    public void upgrade() {
        if (!upgraded) {
            upgraded = true;
            name = name + "+";
        }
    }
    public abstract List<GameEvent> use(GameState state, com.cardgame.logic.monsters.AbstractMonster target);
    public abstract AbstractCard makeCopy();
        public AbstractCard withUpgrade() {
        AbstractCard c = makeCopy();
        c.upgrade();
        return c;
    }

    protected void dealDamage(GameState state, com.cardgame.logic.monsters.AbstractMonster target, int damage, List<GameEvent> events) {
        int atkBoost = RunManager.getInstance().getTotalAttackBoost();
        int rawDamage = damage + atkBoost;
        rawDamage += state.playerStatus.get(StatusEffect.STRENGTH);
        if (state.playerStatus.has(StatusEffect.WEAK)) rawDamage = (int)(rawDamage * 0.75f);
        if (target.status.has(StatusEffect.VULNERABLE)) rawDamage = (int)(rawDamage * 1.5f);

        int damageDealt = 0;
        if (target.block > 0) {
            if (rawDamage <= target.block) {
                target.block -= rawDamage;
                rawDamage = 0;
            } else {
                rawDamage -= target.block;
                target.block = 0;
                target.currentHp -= rawDamage;
                damageDealt = rawDamage;
            }
        } else {
            target.currentHp -= rawDamage;
            damageDealt = rawDamage;
        }
        if (damageDealt > 0) {
            events.add(new DamageDealtEvent("player", "monster", damageDealt));
        }
    }

    protected void gainBlock(GameState state, int blockAmount, List<GameEvent> events) {
        int defBoost = RunManager.getInstance().getTotalDefenceBoost();
        int blockGained = blockAmount + defBoost;
        blockGained += state.playerStatus.get(StatusEffect.DEXTERITY);
        state.playerBlock += blockGained;
        events.add(new BlockGainedEvent("player", blockGained));
    }
}
