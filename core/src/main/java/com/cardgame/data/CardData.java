package com.cardgame.data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable template for a card, loaded once from cards.json.
 * <p>
 * HARD RULE: this class (and all of data/) must NOT import any libGDX class.
 * It compiles and runs inside a plain JUnit test with no graphics context.
 */
public final class CardData {

    private final String id;
    private final String name;
    private final int manaCost;
    private final int attack;
    private final int health;
    private final List<String> abilityIds;
    private final String description;

    // No-arg constructor required by Gson for deserialization
    public CardData() {
        this("", "", 0, 0, 0, List.of(), "");
    }

    public CardData(String id,
                    String name,
                    int manaCost,
                    int attack,
                    int health,
                    List<String> abilityIds,
                    String description) {
        this.id          = Objects.requireNonNull(id,          "id must not be null");
        this.name        = Objects.requireNonNull(name,        "name must not be null");
        this.manaCost    = manaCost;
        this.attack      = attack;
        this.health      = health;
        this.abilityIds  = abilityIds == null ? List.of() : Collections.unmodifiableList(abilityIds);
        this.description = description == null ? "" : description;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String       id()          { return id;          }
    public String       name()        { return name;        }
    public int          manaCost()    { return manaCost;    }
    public int          attack()      { return attack;      }
    public int          health()      { return health;      }
    public List<String> abilityIds()  { return abilityIds;  }
    public String       description() { return description; }

    @Override
    public String toString() {
        return String.format("CardData[id=%s, name=%s, cost=%d, atk=%d, hp=%d, abilities=%s]",
                id, name, manaCost, attack, health, abilityIds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardData c)) return false;
        return id.equals(c.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
