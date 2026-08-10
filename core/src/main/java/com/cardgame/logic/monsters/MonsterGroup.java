package com.cardgame.logic.monsters;

import java.util.ArrayList;
import java.util.List;

public class MonsterGroup {
    public List<AbstractMonster> monsters = new ArrayList<>();
    
    public MonsterGroup(AbstractMonster... mList) {
        for(AbstractMonster m : mList) {
            monsters.add(m);
        }
    }
    
    public boolean areMonstersBasicallyDead() {
        for(AbstractMonster m : monsters) {
            if (m.currentHp > 0) return false;
        }
        return true;
    }
    
    public void disposeAll() {
        for(AbstractMonster m : monsters) m.dispose();
    }
}
