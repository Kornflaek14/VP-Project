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
    
    // Core stats
    private final int attack;
    private final int health;
    private final int bloodCost;
    private final int boneCost;
    
    // Thematic properties
    private final CardType cardType;
    private final UnitArchetype unitArchetype;
    private final AffinityType affinityType;
    private final String imagePath;
    
    // Abilities & Effects
    private final List<String> abilityIds;
    private final List<StatusEffectData> statusEffects;
    private final String description;

    // No-arg constructor required by Gson for deserialization
    public CardData() {
        this("", "", 0, 0, 0, 0,
             CardType.UNIT, UnitArchetype.STANDARD, AffinityType.NEUTRAL, 
             null, 
             List.of(), List.of(), "");
    }

    public CardData(String id,
                    String name,
                    int attack,
                    int health,
                    int bloodCost,
                    int boneCost,
                    CardType cardType,
                    UnitArchetype unitArchetype,
                    AffinityType affinityType,
                    String imagePath,
                    List<String> abilityIds,
                    List<StatusEffectData> statusEffects,
                    String description) {
        this.id            = Objects.requireNonNull(id,   "id must not be null");
        this.name          = Objects.requireNonNull(name, "name must not be null");
        this.attack        = attack;
        this.health        = health;
        this.bloodCost     = Math.max(0, bloodCost);
        this.boneCost      = Math.max(0, boneCost);
        
        this.cardType      = cardType == null ? CardType.UNIT : cardType;
        this.unitArchetype = unitArchetype == null ? UnitArchetype.STANDARD : unitArchetype;
        this.affinityType  = affinityType == null ? AffinityType.NEUTRAL : affinityType;
        this.imagePath     = imagePath;
        
        this.abilityIds    = abilityIds == null ? List.of() : Collections.unmodifiableList(abilityIds);
        this.statusEffects = statusEffects == null ? List.of() : Collections.unmodifiableList(statusEffects);
        this.description   = description == null ? "" : description;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String       id()            { return id;            }
    public String       name()          { return name;          }
    public int          attack()        { return attack;        }
    public int          health()        { return health;        }
    public int          bloodCost()     { return bloodCost;     }
    public int          boneCost()      { return boneCost;      }
    public CardType     cardType()      { return cardType;      }
    public UnitArchetype unitArchetype(){ return unitArchetype; }
    public AffinityType affinityType()  { return affinityType;  }
    public String       imagePath()     { return imagePath;     }
    public List<String> abilityIds()    { return abilityIds;    }
    public List<StatusEffectData> statusEffects() { return statusEffects; }
    public String       description()   { return description;   }

    @Override
    public String toString() {
        return String.format("CardData[id=%s, name=%s, blood=%d, bones=%d, attack=%d, health=%d]",
                id, name, bloodCost, boneCost, attack, health);
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

