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
 * Scene2D {@link Group} that renders 6 fixed hand card slots for a player.
 * <p>
 * Manages {@link CardActor} children, keeping them synchronized with
 * {@link GameState.PlayerState#hand}. Cards use drag interaction.
 */
public class HandArea extends Group {

    private final int playerIndex;
    private final CardActor.CardInteractionCallback callback;
    private final List<CardActor> cardActors = new ArrayList<>();
    private final Texture emptySlotTex;

    public HandArea(int playerIndex, CardActor.CardInteractionCallback callback) {
        this.playerIndex = playerIndex;
        this.callback = callback;

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.15f, 0.18f, 0.25f, 0.35f));
        pm.fill();
        this.emptySlotTex = new Texture(pm);
        pm.dispose();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Draw empty slot backgrounds first
        float alpha = parentAlpha * getColor().a;
        batch.setColor(1, 1, 1, alpha);

        float totalWidth = Constants.MAX_HAND_SIZE * (Constants.CARD_WIDTH + Constants.CARD_GAP) - Constants.CARD_GAP;
        float startX = (getWidth() - totalWidth) / 2f;

        for (int i = 0; i < Constants.MAX_HAND_SIZE; i++) {
            float slotX = startX + i * (Constants.CARD_WIDTH + Constants.CARD_GAP);
            float slotY = (getHeight() - Constants.CARD_HEIGHT) / 2f;
            batch.draw(emptySlotTex, getX() + slotX, getY() + slotY, Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
        }

        super.draw(batch, parentAlpha);
    }

    /**
     * Synchronize the visual hand actors with the game state hand.
     */
    public void syncWithState(GameState state) {
        List<CardInstance> hand = state.getHand(playerIndex);

        // Remove actors for cards no longer in hand
        List<CardActor> toRemove = new ArrayList<>();
        for (CardActor ca : cardActors) {
            if (!hand.contains(ca.getCard())) toRemove.add(ca);
        }
        for (CardActor ca : toRemove) {
            removeActor(ca);
            cardActors.remove(ca);
            ca.dispose();
        }

        // Add actors for newly-drawn cards
        for (CardInstance ci : hand) {
            boolean exists = cardActors.stream().anyMatch(ca -> ca.getCard() == ci);
            if (!exists) {
                CardActor ca = new CardActor(ci, callback);
                ca.setSize(Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
                addActor(ca);
                cardActors.add(ca);
            }
        }

        // Layout in hand order
        float totalWidth = hand.size() * (Constants.CARD_WIDTH + Constants.CARD_GAP) - Constants.CARD_GAP;
        float startX = (getWidth() - totalWidth) / 2f;

        for (int i = 0; i < hand.size(); i++) {
            CardInstance ci = hand.get(i);
            final int index = i;
            cardActors.stream()
                      .filter(ca -> ca.getCard() == ci)
                      .findFirst()
                      .ifPresent(ca -> {
                          float x = startX + index * (Constants.CARD_WIDTH + Constants.CARD_GAP);
                          float y = (getHeight() - Constants.CARD_HEIGHT) / 2f;
                          ca.setPosition(x, y);
                          ca.saveHomePosition();
                      });
        }
    }

    /** Find the CardActor for a given card instance, or null. */
    public CardActor findActorFor(CardInstance card) {
        for (CardActor ca : cardActors) {
            if (ca.getCard() == card) return ca;
        }
        return null;
    }

    /** Clear all selection highlights. */
    public void clearSelection() {
        cardActors.forEach(ca -> ca.setSelected(false));
    }

    public List<CardActor> getCardActors() {
        return cardActors;
    }

    /** Dispose all card actor resources. */
    public void disposeAll() {
        for (CardActor ca : cardActors) ca.dispose();
        cardActors.clear();
        emptySlotTex.dispose();
    }
}
