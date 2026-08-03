package com.cardgame.logic.ai;

import com.cardgame.data.CardType;
import com.cardgame.data.UnitArchetype;
import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.logic.actions.GameAction;
import com.cardgame.logic.actions.PlayCardAction;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleAI {
    private final int aiPlayerIndex = 1;

    public GameAction getNextAction(GameState state) {
        if (state.getCurrentPlayer() != aiPlayerIndex) {
            return null; // Not our turn
        }

        GameState.PlayerState aiState = state.getPlayer(aiPlayerIndex);

        int aiBoardSize = 0;
        for (CardInstance c : aiState.board) {
            if (c != null) aiBoardSize++;
        }

        // 1. Try to play cards
        if (aiBoardSize < Constants.MAX_BOARD_SIZE) {
            for (CardInstance card : aiState.hand) {
                if (card.getTemplate().boneCost() <= aiState.bones && card.getTemplate().bloodCost() <= aiState.sacrificeCredit) {
                    if (card.getTemplate().cardType() == CardType.UNIT) {
                        return new PlayCardAction(aiPlayerIndex, card, -1, null);
                    }
                }
            }
        }

        // Simple AI doesn't sacrifice for now.
        return null; // No actions left
    }
}
