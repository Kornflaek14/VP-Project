package com.cardgame.logic.monsters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonsterGroup {
    public List<AbstractMonster> monsters = new ArrayList<>();
    
    public MonsterGroup(AbstractMonster... mList) {
        List<AbstractMonster> input = new ArrayList<>();
        AbstractMonster elite = null;
        for (AbstractMonster m : mList) {
            if (m != null) {
                if (m.isElite() && elite == null) {
                    elite = m;
                }
                input.add(m);
            }
        }
        
        // Elite monsters can never appear in pairs or alongside other enemies
        if (elite != null) {
            elite.drawX = 1000f;
            elite.drawY = 280f;
            monsters.add(elite);
            return;
        }

        Map<String, Integer> counts = new HashMap<>();
        for (AbstractMonster m : input) {
            counts.put(m.name, counts.getOrDefault(m.name, 0) + 1);
        }
        Map<String, Integer> indices = new HashMap<>();
        for (AbstractMonster m : input) {
            if (counts.get(m.name) > 1) {
                int idx = indices.getOrDefault(m.name, 0);
                indices.put(m.name, idx + 1);
                char letter = (char) ('A' + idx);
                m.name = m.name + " " + letter;
            }
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
