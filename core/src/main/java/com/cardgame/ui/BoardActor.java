package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cardgame.logic.CardInstance;
import com.cardgame.logic.GameState;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Scene2D {@link Group} that lays out exactly {@link Constants#MAX_BOARD_SIZE} slots
 * for one player's board. Draws empty slots and populated {@link CardActor}s.
 */
public class BoardActor extends Group {

    public interface OnSlotClickCallback {
        void onSlotClicked(int slotIndex);
    }

    private final int playerIndex;
    private final CardActor.OnClickCallback onCardClick;
    private final OnSlotClickCallback onSlotClick;
    
    private final List<CardActor> cardActors = new ArrayList<>();
    private final Texture emptySlotTex;

    public BoardActor(int playerIndex, CardActor.OnClickCallback onCardClick, OnSlotClickCallback onSlotClick) {
        this.playerIndex = playerIndex;
        this.onCardClick = onCardClick;
        this.onSlotClick = onSlotClick;
        
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.2f, 0.2f, 0.3f, 0.5f));
        pm.fill();
        this.emptySlotTex = new Texture(pm);
        pm.dispose();
        
        // Add click listener to the entire board background to capture empty slot clicks
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.getTarget() == BoardActor.this) {
                    float totalWidth = Constants.MAX_BOARD_SIZE * (Constants.CARD_WIDTH + Constants.CARD_GAP) - Constants.CARD_GAP;
                    float startX = (getWidth() - totalWidth) / 2f;
                    
                    for (int i = 0; i < Constants.MAX_BOARD_SIZE; i++) {
                        float slotX = startX + i * (Constants.CARD_WIDTH + Constants.CARD_GAP);
                        float slotY = (getHeight() - Constants.CARD_HEIGHT) / 2f;
                        if (x >= slotX && x <= slotX + Constants.CARD_WIDTH &&
                            y >= slotY && y <= slotY + Constants.CARD_HEIGHT) {
                            if (onSlotClick != null) {
                                onSlotClick.onSlotClicked(i);
                            }
                            break;
                        }
                    }
                }
            }
        });
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
            float slotY = (getHeight() - Constants.CARD_HEIGHT) / 2f;
            batch.draw(emptySlotTex, getX() + slotX, getY() + slotY, Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
        }
        
        super.draw(batch, parentAlpha);
    }

    public void syncWithState(GameState state) {
        CardInstance[] board = state.getBoard(playerIndex);

        // Remove actors for cards no longer on board
        List<CardActor> toRemove = new ArrayList<>();
        for (CardActor ca : cardActors) {
            boolean found = false;
            for (CardInstance c : board) {
                if (c == ca.getCard()) {
                    found = true;
                    break;
                }
            }
            if (!found) toRemove.add(ca);
        }
        for (CardActor ca : toRemove) {
            cardActors.remove(ca);
            ca.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(0.3f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.run(ca::dispose),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor()
            ));
        }

        // Add actors for newly-placed cards
        for (int i = 0; i < board.length; i++) {
            CardInstance ci = board[i];
            if (ci != null) {
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
                              float yPos = (getHeight() - Constants.CARD_HEIGHT) / 2f;
                              ca.setPosition(xPos, yPos);
                          });
            }
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
        return super.remove();
    }
}
