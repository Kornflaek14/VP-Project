package com.cardgame.logic.rooms;
import com.cardgame.CardBattlerGame;

public class BossRoom extends AbstractRoom {
    @Override
    public void onPlayerEntry(CardBattlerGame game) {
        // For now, immediately transition to the hardcoded screen.
        // In later phases, we will shift to RoomPhase state checking.
        game.setScreen(new com.cardgame.screens.BattleScreen(game, new com.cardgame.logic.monsters.MonsterGroup(new com.cardgame.logic.monsters.FleshAmalgam(1000f, 250f))));
    }
}
