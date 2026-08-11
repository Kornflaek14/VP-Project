package com.cardgame.data;

/**
 * Immutable relic template loaded from relics.json.
 */
public final class RelicData {
    private final String id;
    private final String name;
    private final int attackBoost;
    private final int defenceBoost;
    private final int hpBoost;
    private final int energyBoost;
    private final String image;

    public RelicData() {
        this("", "", 0, 0, 0, 0, "");
    }

    public RelicData(String id, String name, int attackBoost, int defenceBoost,
                     int hpBoost, int energyBoost, String image) {
        this.id = id;
        this.name = name;
        this.attackBoost = attackBoost;
        this.defenceBoost = defenceBoost;
        this.hpBoost = hpBoost;
        this.energyBoost = energyBoost;
        this.image = image;
    }

    public String id()           { return id;           }
    public String name()         { return name;         }
    public int    attackBoost()  { return attackBoost;  }
    public int    defenceBoost() { return defenceBoost; }
    public int    hpBoost()      { return hpBoost;      }
    public int    energyBoost()  { return energyBoost;  }
    public String image()        { return image;        }
}
