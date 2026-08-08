package com.cardgame.logic.rooms;
import com.cardgame.CardBattlerGame;

public abstract class AbstractRoom {
    public RoomPhase phase = RoomPhase.INCOMPLETE;

    public abstract void onPlayerEntry(CardBattlerGame game);
    
    public void update() {}
}
