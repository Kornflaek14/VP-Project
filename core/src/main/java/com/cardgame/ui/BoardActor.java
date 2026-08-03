package com.cardgame.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Scene2D {@link Group} that lays out {@link CardActor}s for one player's board.
 * <p>
 * Call {@link #syncWithState} every frame (or after any state change) to rebuild
 * the actor list from the live {@link GameState}.
 * <p>
 * Visuals and input only — no game-rule logic.
 */
public class BoardActor extends Group {

    private final int playerIndex;
    private final CardActor.OnClickCallback onCardClick;
    private final List<CardActor> cardActors = new ArrayList<>();

    public BoardActor(int playerIndex, CardActor.OnClickCallback onCardClick) {
        this.playerIndex  = playerIndex;
        this.onCardClick  = onCardClick;
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Rebuilds child actors to mirror the current board in {@code state}.
     * Existing actors that match a surviving {@link CardInstance} are reused;
     * dead-card actors are disposed.
     */
    public void syncWithState(GameState state) {
        List<CardInstance> board = state.getBoard(playerIndex);

        // Remove actors for cards no longer on board
        List<CardActor> toRemove = new ArrayList<>();
        for (CardActor ca : cardActors) {
            if (!board.contains(ca.getCard())) toRemove.add(ca);
        }
        for (CardActor ca : toRemove) {
            removeActor(ca);
            cardActors.remove(ca);
            ca.dispose();
        }

        // Add actors for newly-placed cards
        for (CardInstance ci : board) {
            boolean exists = cardActors.stream().anyMatch(ca -> ca.getCard() == ci);
            if (!exists) {
                CardActor actor = new CardActor(ci, onCardClick);
                actor.setSize(Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
                addActor(actor);
                cardActors.add(actor);
            }
        }

        // Re-layout in board order
        float totalWidth = board.size() * (Constants.CARD_WIDTH + Constants.CARD_GAP) - Constants.CARD_GAP;
        float startX     = (getWidth() - totalWidth) / 2f;

        for (int i = 0; i < board.size(); i++) {
            CardInstance ci    = board.get(i);
            final int    index = i;
            cardActors.stream()
                      .filter(ca -> ca.getCard() == ci)
                      .findFirst()
                      .ifPresent(ca -> {
                          float xPos = startX + index * (Constants.CARD_WIDTH + Constants.CARD_GAP);
                          float yPos = (getHeight() - Constants.CARD_HEIGHT) / 2f;
                          ca.setPosition(xPos, yPos);
                      });
        }
    }

    /**
     * Marks the given actor as selected and deselects all others.
     */
    public void selectOnly(CardActor target) {
        for (CardActor ca : cardActors) {
            ca.setSelected(ca == target);
        }
    }

    /** Clears the selection on all cards. */
    public void clearSelection() {
        cardActors.forEach(ca -> ca.setSelected(false));
    }

    @Override
    public boolean remove() {
        for (CardActor ca : cardActors) ca.dispose();
        cardActors.clear();
        return super.remove();
    }
}
