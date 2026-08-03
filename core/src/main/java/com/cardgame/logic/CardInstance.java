package com.cardgame.logic;

import com.cardgame.data.CardData;

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
    private boolean exhausted;   // true = summoning sickness or already attacked
    private boolean taunt;

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
    public boolean  isExhausted()       { return exhausted;      }
    public boolean  hasTaunt()          { return taunt;          }
    public boolean  isDead()            { return currentHealth <= 0; }

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
    public void setTaunt(boolean taunt)          { this.taunt     = taunt;     }

    @Override
    public String toString() {
        return String.format("CardInstance[%s '%s' hp=%d/%d%s%s]",
                instanceId.substring(0, 6),
                template.name(),
                currentHealth,
                template.health(),
                taunt     ? " TAUNT"    : "",
                exhausted ? " EXHAUSTED" : "");
    }
}
