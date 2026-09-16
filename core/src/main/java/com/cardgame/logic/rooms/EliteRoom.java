package com.cardgame.logic.rooms;

import com.cardgame.CardBattlerGame;
import com.cardgame.logic.monsters.AbstractMonster;
import com.cardgame.logic.monsters.HeadNurse;
import com.cardgame.logic.monsters.MaskedPatient;
import com.cardgame.logic.monsters.MonsterGroup;
import java.util.concurrent.ThreadLocalRandom;

public class EliteRoom extends AbstractRoom {
    public MonsterGroup monsters;

    @Override
    public void onPlayerEntry(CardBattlerGame game) {
        this.phase = RoomPhase.COMBAT;
        AbstractMonster elite = ThreadLocalRandom.current().nextBoolean()
                ? new MaskedPatient(1000f, 280f)
                : new HeadNurse(1000f, 280f);
        this.monsters = new MonsterGroup(elite);
        game.setScreen(new com.cardgame.screens.BattleScreen(game, this.monsters));
    }
}
