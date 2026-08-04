package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Scene2D {@link Group} that lays out exactly {@link Constants#MAX_BOARD_SIZE} slots
 * for one player's board. Draws empty slots, populated {@link CardActor}s,
 * and drop-target highlights during drag.
 */
public class BoardActor extends Group {

    private final int playerIndex;
    private final CardActor.OnClickCallback onCardClick;
    
    private final List<CardActor> cardActors = new ArrayList<>();
    private final Texture emptySlotTex;
    private final Texture highlightTex;
    
    /** Per-slot highlight state for drag-drop targets. */
    private final boolean[] slotHighlighted = new boolean[Constants.MAX_BOARD_SIZE];

    public BoardActor(int playerIndex, CardActor.OnClickCallback onCardClick) {
        this.playerIndex = playerIndex;
        this.onCardClick = onCardClick;
        
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.2f, 0.2f, 0.3f, 0.5f));
        pm.fill();
        this.emptySlotTex = new Texture(pm);
        
        pm.setColor(new Color(0.2f, 0.85f, 0.3f, 0.35f));
        pm.fill();
        this.highlightTex = new Texture(pm);
        
        pm.dispose();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Draw empty slot backgrounds FIRST, so cards draw on top
        float alpha = parentAlpha * getColor().a;
        batch.setColor(1, 1, 1, alpha);
        
        float totalWidth = Constants.MAX_BOARD_SIZE * (Constants.CARD_WIDTH + Constants.CARD_GAP) - Constants.CARD_GAP;
        float startX = (getWidth() - totalWidth) / 2f;
        
        for (int i = 0; i < Constants.MAX_BOARD_SIZE; i++) {
            float slotX = startX + i * (Constants.CARD_WIDTH + Constants.CARD_GAP);
            float slotY = (playerIndex == 1) ? 20f : (getHeight() - Constants.CARD_HEIGHT) / 2f;
            
            batch.setColor(1, 1, 1, alpha);
            batch.draw(emptySlotTex, getX() + slotX, getY() + slotY, Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
            
            if (playerIndex == 1) {
                // Draw queue row empty slot
                float queueY = slotY + Constants.CARD_HEIGHT + 10f;
                batch.setColor(1, 1, 1, alpha * 0.5f);
                batch.draw(emptySlotTex, getX() + slotX, getY() + queueY, Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
            }
            
            // Draw highlight overlay for valid drop targets
            if (slotHighlighted[i]) {
                batch.setColor(1, 1, 1, alpha);
                batch.draw(highlightTex, getX() + slotX, getY() + slotY, Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
            }
        }
        
        super.draw(batch, parentAlpha);
    }

    public void syncWithState(GameState state) {
        CardInstance[] board = state.getBoard(playerIndex);
        CardInstance[] queue = state.getPlayer(playerIndex).queueBoard;

        // Combine into one list for tracking
        List<CardInstance> allCards = new ArrayList<>();
        for (CardInstance c : board) if (c != null) allCards.add(c);
        if (playerIndex == 1) {
            for (CardInstance c : queue) if (c != null) allCards.add(c);
        }

        // Remove actors for cards no longer on board/queue
        List<CardActor> toRemove = new ArrayList<>();
        for (CardActor ca : cardActors) {
            if (!allCards.contains(ca.getCard())) {
                toRemove.add(ca);
            }
        }
        for (CardActor ca : toRemove) {
            cardActors.remove(ca);
            ca.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(15, 15, 0.05f),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(-30, -30, 0.05f),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(30, -15, 0.05f),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(-15, 30, 0.05f)
                    ),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(0.2f)
                ),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.run(ca::dispose),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor()
            ));
        }

        // Add actors for newly-placed cards
        for (CardInstance ci : allCards) {
            boolean exists = cardActors.stream().anyMatch(ca -> ca.getCard() == ci);
            if (!exists) {
                CardActor actor = new CardActor(ci, onCardClick);
                actor.setSize(Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
                actor.getColor().a = 0f;
                actor.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(0.3f));
                addActor(actor);
                cardActors.add(actor);
            }
        }

        // Layout in board order
        float totalWidth = Constants.MAX_BOARD_SIZE * (Constants.CARD_WIDTH + Constants.CARD_GAP) - Constants.CARD_GAP;
        float startX     = (getWidth() - totalWidth) / 2f;

        for (int i = 0; i < board.length; i++) {
            CardInstance ci = board[i];
            if (ci != null) {
                final int index = i;
                cardActors.stream()
                          .filter(ca -> ca.getCard() == ci)
                          .findFirst()
                          .ifPresent(ca -> {
                              float xPos = startX + index * (Constants.CARD_WIDTH + Constants.CARD_GAP);
                              float yPos = (playerIndex == 1) ? 20f : (getHeight() - Constants.CARD_HEIGHT) / 2f;
                              ca.setPosition(xPos, yPos);
                              ca.getColor().a = 1f; // opaque
                          });
            }
        }

        if (playerIndex == 1) {
            for (int i = 0; i < queue.length; i++) {
                CardInstance ci = queue[i];
                if (ci != null) {
                    final int index = i;
                    cardActors.stream().filter(ca -> ca.getCard() == ci).findFirst().ifPresent(ca -> {
                        float xPos = startX + index * (Constants.CARD_WIDTH + Constants.CARD_GAP);
                        float yPos = 20f + Constants.CARD_HEIGHT + 10f;
                        ca.setPosition(xPos, yPos);
                        ca.getColor().a = 0.5f; // translucent for queue
                    });
                }
            }
        }
    }

    // ── Drop-target highlight API ──────────────────────────────────────────────

    /**
     * Returns the board slot index at the given stage coordinates, or -1 if none.
     */
    public int getSlotIndexAt(float stageX, float stageY) {
        // Convert stage coordinates to local
        com.badlogic.gdx.math.Vector2 local = stageToLocalCoordinates(
                new com.badlogic.gdx.math.Vector2(stageX, stageY));
        float x = local.x;
        float y = local.y;
        
        float totalWidth = Constants.MAX_BOARD_SIZE * (Constants.CARD_WIDTH + Constants.CARD_GAP) - Constants.CARD_GAP;
        float startX = (getWidth() - totalWidth) / 2f;
        
        for (int i = 0; i < Constants.MAX_BOARD_SIZE; i++) {
            float slotX = startX + i * (Constants.CARD_WIDTH + Constants.CARD_GAP);
            float slotY = (playerIndex == 1) ? 20f : (getHeight() - Constants.CARD_HEIGHT) / 2f;
            if (x >= slotX && x <= slotX + Constants.CARD_WIDTH &&
                y >= slotY && y <= slotY + Constants.CARD_HEIGHT) {
                return i;
            }
        }
        return -1;
    }

    /** Highlight a specific slot as a valid drop target. */
    public void setSlotHighlighted(int slot, boolean hl) {
        if (slot >= 0 && slot < slotHighlighted.length) {
            slotHighlighted[slot] = hl;
        }
    }

    /** Highlight all empty slots as valid drop targets. */
    public void highlightEmptySlots(GameState state) {
        CardInstance[] board = state.getBoard(playerIndex);
        for (int i = 0; i < Constants.MAX_BOARD_SIZE; i++) {
            slotHighlighted[i] = (board[i] == null);
        }
    }

    /** Clear all slot highlights. */
    public void clearHighlights() {
        for (int i = 0; i < slotHighlighted.length; i++) {
            slotHighlighted[i] = false;
        }
    }

    public void selectOnly(CardActor target) {
        for (CardActor ca : cardActors) {
            ca.setSelected(ca == target);
        }
    }

    public void clearSelection() {
        cardActors.forEach(ca -> ca.setSelected(false));
    }

    @Override
    public boolean remove() {
        for (CardActor ca : cardActors) ca.dispose();
        cardActors.clear();
        emptySlotTex.dispose();
        highlightTex.dispose();
        return super.remove();
    }
}
