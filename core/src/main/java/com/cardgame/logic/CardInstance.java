package com.cardgame.logic;

import com.cardgame.data.CardData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mutable runtime wrapper around an immutable {@link CardData} template.
 * <p>
 * Each instance has a unique {@link #instanceId} so two copies of the same
 * card template on the board can be distinguished.
 * <p>
 * HARD RULE: no libGDX imports.
 */
public final class CardInstance {

    private final CardData template;
    private final String   instanceId;
    private final int      ownerIndex;   // 0 or 1

    // ── Mutable runtime state ──────────────────────────────────────────────────
    private int     currentHealth;
    private int     attackBonus; // Buffs from items/auras
    private boolean exhausted;   // true = summoning sickness or already attacked
    private boolean taunt;
    
    // Status effects
    private final List<ActiveEffect> activeEffects = new ArrayList<>();
    private boolean frozen;
    private int     thornsValue;
    private boolean rangedThisTurn; // true = bypass retaliation this turn

    // ── Constructor ────────────────────────────────────────────────────────────

    public CardInstance(CardData template, int ownerIndex) {
        this.template      = template;
        this.ownerIndex    = ownerIndex;
        this.instanceId    = UUID.randomUUID().toString();
        this.currentHealth = template.health();
        this.exhausted     = true;  // summoning sickness by default
        this.taunt         = false;
    }

    // ── Queries ────────────────────────────────────────────────────────────────

    public CardData getTemplate()       { return template;       }
    public String   getInstanceId()     { return instanceId;     }
    public int      getOwnerIndex()     { return ownerIndex;     }
    
    public int      getCurrentHealth()  { return currentHealth;  }
    public int      getEffectiveAttack(){ return template.attack() + attackBonus; }
    
    public boolean  isExhausted()       { return exhausted;      }
    public boolean  hasTaunt()          { return taunt;          }
    public boolean  isDead()            { return currentHealth <= 0; }
    
    public boolean  isFrozen()          { return frozen;         }
    public int      getThornsValue()    { return thornsValue;    }
    public boolean  isRangedThisTurn()  { return rangedThisTurn; }
    
    public List<ActiveEffect> getActiveEffects() { return activeEffects; }

    // ── Mutations (called only by logic classes) ───────────────────────────────

    public void dealDamage(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Damage cannot be negative");
        currentHealth -= amount;
    }

    public void heal(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Heal amount cannot be negative");
        currentHealth = Math.min(template.health(), currentHealth + amount);
    }

    public void setExhausted(boolean exhausted) { this.exhausted = exhausted; }
    public void setTaunt(boolean taunt)         { this.taunt     = taunt;     }
    public void setFrozen(boolean frozen)       { this.frozen    = frozen;    }
    public void setThornsValue(int thorns)      { this.thornsValue = thorns;  }
    public void setRangedThisTurn(boolean r)    { this.rangedThisTurn = r;    }
    public void setAttackBonus(int bonus)       { this.attackBonus = bonus;   }
    public void addAttackBonus(int amount)      { this.attackBonus += amount; }

    @Override
    public String toString() {
        return String.format("CardInstance[%s '%s' hp=%d/%d%s%s%s]",
                instanceId.substring(0, 6),
                template.name(),
                currentHealth,
                template.health(),
                taunt     ? " TAUNT"    : "",
                exhausted ? " EXHAUSTED" : "",
                frozen    ? " FROZEN"   : "");
    }
}
