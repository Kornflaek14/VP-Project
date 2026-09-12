package com.cardgame.logic.rooms;
import com.cardgame.CardBattlerGame;

public class TreasureRoom extends AbstractRoom {
    @Override
    public void onPlayerEntry(CardBattlerGame game) {
        // For now, immediately transition to the hardcoded screen.
        // In later phases, we will shift to RoomPhase state checking.
        game.setScreen(new com.cardgame.screens.TreasureScreen(game));
    }
}
