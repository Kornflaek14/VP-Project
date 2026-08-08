package com.cardgame.logic.rooms;

import com.cardgame.CardBattlerGame;
import com.cardgame.logic.monsters.*;
import java.util.concurrent.ThreadLocalRandom;

public class MonsterRoom extends AbstractRoom {
    public MonsterGroup monsters;
    
    @Override
    public void onPlayerEntry(CardBattlerGame game) {
        this.phase = RoomPhase.COMBAT;
        
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < 25) {
            monsters = new MonsterGroup(new FrenziedPatient(1000f, 250f));
        } else if (roll < 50) {
            monsters = new MonsterGroup(new FleshAmalgam(1000f, 250f));
        } else if (roll < 75) {
            monsters = new MonsterGroup(
                new CrawlingEye(850f, 250f),
                new CrawlingEye(1150f, 250f)
            );
        } else {
            monsters = new MonsterGroup(
                new FleshAmalgam(850f, 250f),
                new CrawlingEye(1150f, 250f)
            );
        }
        
        
        game.setScreen(new com.cardgame.screens.BattleScreen(game, monsters));
    }
}
