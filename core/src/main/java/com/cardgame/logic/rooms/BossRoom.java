package com.cardgame.logic.rooms;
import com.cardgame.CardBattlerGame;

public class BossRoom extends AbstractRoom {
    @Override
    public void onPlayerEntry(CardBattlerGame game) {
        // Boss fight at the end of the level
        game.setScreen(new com.cardgame.screens.BattleScreen(game, new com.cardgame.logic.monsters.MonsterGroup(new com.cardgame.logic.monsters.Boss(1000f, 250f)), true));
    }
}
