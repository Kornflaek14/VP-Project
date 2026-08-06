package com.cardgame.data;

import java.util.Objects;

/**
 * Immutable template for a card, loaded once from cards.json.
 * Follows Slay the Spire schema: attack cards deal damage, skill cards give block.
 */
public final class CardData {

    private final String id;
    private final String name;
    private final int energyCost;
    private final String character; // "Ironclad", "Defect", "Watcher"
    private final int damage;
    private final int defence;      // block amount
    private final String description;
    private final String image;     // relative path under assets/
    private final CardType cardType;
    private final String specialEffect; // e.g. "ADD_COPY_TO_DISCARD", "APPLY_VULNERABLE_2"
    private final boolean upgraded;     // true when card has been smithed

    /** No-arg constructor for Gson. */
    public CardData() {
        this("", "", 0, "", 0, 0, "", "", CardType.ATTACK, "", false);
    }

    public CardData(String id, String name, int energyCost, String character,
                    int damage, int defence, String description, String image,
                    CardType cardType, String specialEffect, boolean upgraded) {
        this.id          = Objects.requireNonNull(id);
        this.name        = Objects.requireNonNull(name);
        this.energyCost  = Math.max(0, energyCost);
        this.character   = character == null ? "" : character;
        this.damage      = Math.max(0, damage);
        this.defence     = Math.max(0, defence);
        this.description = description == null ? "" : description;
        this.image       = image == null ? "" : image;
        this.cardType    = cardType == null ? CardType.ATTACK : cardType;
        this.specialEffect = specialEffect == null ? "" : specialEffect;
        this.upgraded    = upgraded;
    }

    /**
     * Convenience constructor used by existing code — no specialEffect.
     */
    public CardData(String id, String name, int energyCost, String character,
                    int damage, int defence, String description, String image,
                    CardType cardType) {
        this(id, name, energyCost, character, damage, defence, description, image, cardType, "", false);
    }

    // ── Accessors ─────────────────────────────────────────────
    public String   id()            { return id;            }
    public String   name()          { return name;          }
    public int      energyCost()    { return energyCost;    }
    public String   character()     { return character;     }
    public int      damage()        { return damage;        }
    public int      defence()       { return defence;       }
    public String   description()   { return description;   }
    public String   image()         { return image;         }
    public CardType cardType()      { return cardType;      }
    public String   specialEffect() { return specialEffect; }
    public boolean  isUpgraded()    { return upgraded;      }

    /**
     * Returns an upgraded copy of this card: +3 damage or +3 defence,
     * name gets a "+" suffix, and upgraded flag is set.
     */
    public CardData withUpgrade() {
        int newDmg = damage > 0 ? damage + 3 : damage;
        int newDef = defence > 0 ? defence + 3 : defence;
        String upgradedName = upgraded ? name : name + "+";
        String upgradedDesc = description + (upgraded ? "" : " (Upgraded)");
        return new CardData(id, upgradedName, energyCost, character,
                newDmg, newDef, upgradedDesc, image, cardType, specialEffect, true);
    }

    @Override
    public String toString() {
        return String.format("CardData[%s '%s' cost=%d dmg=%d def=%d %s]",
                id, name, energyCost, damage, defence, cardType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardData c)) return false;
        return id.equals(c.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
