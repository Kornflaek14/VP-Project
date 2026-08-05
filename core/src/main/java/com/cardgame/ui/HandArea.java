package com.cardgame.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.cardgame.data.CardData;
import com.cardgame.logic.GameState;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the player's hand of cards in a fan layout.
 */
public class HandArea extends Group {

    private final CardActor.OnClickCallback callback;
    private final List<CardActor> cardActors = new ArrayList<>();

    public HandArea(CardActor.OnClickCallback callback) {
        this.callback = callback;
    }

    public void syncWithState(GameState state) {
        List<CardData> hand = state.hand;

        // Remove old actors
        List<CardActor> toRemove = new ArrayList<>();
        for (CardActor ca : cardActors) {
            if (!hand.contains(ca.getCard())) toRemove.add(ca);
        }
        for (CardActor ca : toRemove) {
            removeActor(ca);
            cardActors.remove(ca);
            ca.dispose();
        }

        // Add new actors
        for (CardData cd : hand) {
            boolean exists = cardActors.stream().anyMatch(ca -> ca.getCard() == cd);
            if (!exists) {
                CardActor ca = new CardActor(cd, callback);
                ca.setSize(Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
                addActor(ca);
                cardActors.add(ca);
            }
        }

        // Fan layout
        float totalWidth = hand.size() * (Constants.CARD_WIDTH + Constants.CARD_GAP) - Constants.CARD_GAP;
        float startX = (Constants.VIEWPORT_WIDTH - totalWidth) / 2f;
        float baseY = 10f; // close to bottom

        for (int i = 0; i < hand.size(); i++) {
            CardData cd = hand.get(i);
            final int index = i;
            cardActors.stream()
                      .filter(ca -> ca.getCard() == cd)
                      .findFirst()
                      .ifPresent(ca -> {
                          float x = startX + index * (Constants.CARD_WIDTH + Constants.CARD_GAP);
                          
                          // Slight arch effect
                          float mid = (hand.size() - 1) / 2f;
                          float distFromMid = Math.abs(index - mid);
                          float yOffset = -distFromMid * distFromMid * 2f; 
                          
                          ca.setPosition(x, baseY + yOffset);
                      });
        }
    }

    public void disposeAll() {
        for (CardActor ca : cardActors) ca.dispose();
        cardActors.clear();
    }
}
