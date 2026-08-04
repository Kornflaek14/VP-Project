package com.cardgame.data;

/**
 * Immutable character template loaded from characters.json.
 */
public final class CharacterData {
    private final String name;
    private final String image;
    private final String portrait;
    private final String buttonImage;
    private final int energy;
    private final int hp;
    private final int gold;
    private final String description;
    private final String startingRelicName;

    public CharacterData() {
        this("", "", "", "", 3, 80, 99, "", "");
    }

    public CharacterData(String name, String image, String portrait, String buttonImage,
                         int energy, int hp, int gold, String description, String startingRelicName) {
        this.name = name;
        this.image = image;
        this.portrait = portrait;
        this.buttonImage = buttonImage;
        this.energy = energy;
        this.hp = hp;
        this.gold = gold;
        this.description = description;
        this.startingRelicName = startingRelicName;
    }

    public String name()              { return name;              }
    public String image()             { return image;             }
    public String portrait()          { return portrait;          }
    public String buttonImage()       { return buttonImage;       }
    public int    energy()            { return energy;            }
    public int    hp()                { return hp;                }
    public int    gold()              { return gold;              }
    public String description()       { return description;       }
    public String startingRelicName() { return startingRelicName; }
}
