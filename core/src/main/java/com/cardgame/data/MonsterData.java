package com.cardgame.data;

/**
 * Immutable monster template loaded from monsters.json.
 */
public final class MonsterData {
    private final String name;
    private final int level;
    private final int hp;
    private final int attackMin;
    private final int attackMax;
    private final int defenceMin;
    private final int defenceMax;
    private final String image;

    public MonsterData() {
        this("", 1, 40, 5, 10, 0, 5, "");
    }

    public MonsterData(String name, int level, int hp, int attackMin, int attackMax,
                       int defenceMin, int defenceMax, String image) {
        this.name = name;
        this.level = level;
        this.hp = hp;
        this.attackMin = attackMin;
        this.attackMax = attackMax;
        this.defenceMin = defenceMin;
        this.defenceMax = defenceMax;
        this.image = image;
    }

    public String name()       { return name;       }
    public int    level()      { return level;      }
    public int    hp()         { return hp;         }
    public int    attackMin()  { return attackMin;  }
    public int    attackMax()  { return attackMax;  }
    public int    defenceMin() { return defenceMin; }
    public int    defenceMax() { return defenceMax; }
    public String image()      { return image;      }
}
