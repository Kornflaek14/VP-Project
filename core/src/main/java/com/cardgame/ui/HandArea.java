package com.cardgame.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.logic.GameState;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the player's hand of cards in a fan layout.
 * - Cards arc in a semicircle with slight rotation per card
 * - Cards overlap when hand is large
 * - Hovered card pops above others via z-ordering
 */
public class HandArea extends Group {

    private static final float MAX_ROTATION_DEG = 12f;   // max tilt at hand edges
    private static final float BASE_Y            = 5f;    // bottom margin
    private static final float ARC_DROP          = 3f;    // px drop per degree of rotation

    private final CardActor.OnDragCallback callback;
    private final List<CardActor> cardActors = new ArrayList<>();

    public HandArea(CardActor.OnDragCallback callback) {
        this.callback = callback;
    }

    public void syncWithState(GameState state) {
        List<AbstractCard> hand = state.hand;

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

        // Add actors for new cards
        for (AbstractCard cd : hand) {
            boolean exists = cardActors.stream().anyMatch(ca -> ca.getCard() == cd);
            if (!exists) {
                CardActor ca = new CardActor(cd, callback);
                // Initial spawn position (bottom right, like a draw pile)
                ca.setPosition(Constants.VIEWPORT_WIDTH - 100, -200);
                addActor(ca);
                cardActors.add(ca);
            }
        }

        layoutHand(hand);
    }

    private void layoutHand(List<AbstractCard> hand) {
        int n = hand.size();
        if (n == 0) return;

        // Overlap cards when hand is large so they all fit on screen
        float cardStep = Math.min(Constants.CARD_WIDTH + Constants.CARD_GAP,
                (Constants.VIEWPORT_WIDTH - 200f) / Math.max(n, 1));

        float totalWidth = cardStep * (n - 1) + Constants.CARD_WIDTH;
        float startX = (Constants.VIEWPORT_WIDTH - totalWidth) / 2f;

        for (int i = 0; i < hand.size(); i++) {
            AbstractCard cd = hand.get(i);
            final int index = i;

            cardActors.stream()
                .filter(ca -> ca.getCard() == cd)
                .findFirst()
                .ifPresent(ca -> {
                    float x = startX + index * cardStep;

                    // Fan rotation: edge cards tilt outward
                    float normalized = n > 1 ? (float)(index) / (n - 1) * 2f - 1f : 0f; // -1 to 1
                    float rotation   = -normalized * MAX_ROTATION_DEG;

                    // Arc: cards at edges drop slightly
                    float yDrop = Math.abs(rotation) * ARC_DROP;
                    float y = BASE_Y - yDrop;

                    // Only update targets if not currently being dragged by the user
                    if (!ca.isDragging()) {
                        ca.targetPos.set(x, y);
                        ca.targetRot = rotation;
                        ca.targetScale = 1f;
                    }
                    ca.setZIndex(index); // natural z-order left to right
                });
        }
    }

    public void disposeAll() {
        for (CardActor ca : cardActors) ca.dispose();
        cardActors.clear();
    }
}
