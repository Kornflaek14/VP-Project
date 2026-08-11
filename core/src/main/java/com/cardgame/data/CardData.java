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

    /** No-arg constructor for Gson. */
    public CardData() {
        this("", "", 0, "", 0, 0, "", "", CardType.ATTACK);
    }

    public CardData(String id, String name, int energyCost, String character,
                    int damage, int defence, String description, String image,
                    CardType cardType) {
        this.id          = Objects.requireNonNull(id);
        this.name        = Objects.requireNonNull(name);
        this.energyCost  = Math.max(0, energyCost);
        this.character   = character == null ? "" : character;
        this.damage      = Math.max(0, damage);
        this.defence     = Math.max(0, defence);
        this.description = description == null ? "" : description;
        this.image       = image == null ? "" : image;
        this.cardType    = cardType == null ? CardType.ATTACK : cardType;
    }

    // ── Accessors ─────────────────────────────────────────────
    public String   id()          { return id;          }
    public String   name()        { return name;        }
    public int      energyCost()  { return energyCost;  }
    public String   character()   { return character;   }
    public int      damage()      { return damage;      }
    public int      defence()     { return defence;     }
    public String   description() { return description; }
    public String   image()       { return image;       }
    public CardType cardType()    { return cardType;    }

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
