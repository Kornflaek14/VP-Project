package com.cardgame.data;

/**
 * Immutable potion template loaded from potions.json.
 */
public final class PotionData {
    private final String id;
    private final String name;
    private final int attackBoost;
    private final int defendBoost;
    private final int hpBoost;
    private final String image;

    public PotionData() {
        this("", "", 0, 0, 0, "");
    }

    public PotionData(String id, String name, int attackBoost, int defendBoost,
                      int hpBoost, String image) {
        this.id = id;
        this.name = name;
        this.attackBoost = attackBoost;
        this.defendBoost = defendBoost;
        this.hpBoost = hpBoost;
        this.image = image;
    }

    public String id()           { return id;           }
    public String name()         { return name;         }
    public int    attackBoost()  { return attackBoost;  }
    public int    defendBoost()  { return defendBoost;  }
    public int    hpBoost()      { return hpBoost;      }
    public String image()        { return image;        }
}
