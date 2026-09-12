package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-screen overlay that displays a scrollable grid of cards from
 * the draw pile or discard pile during combat.
 * Click anywhere outside a card to close.
 */
public class PileViewerOverlay extends Group {

    private final Texture bgTex;
    private final Texture headerBgTex;
    private final BitmapFont titleFont;
    private final BitmapFont countFont;
    private final List<CardActor> cardActors = new ArrayList<>();
    private String title = "";
    private int totalCards = 0;

    private static final float CARD_W = Constants.CARD_WIDTH * 0.7f;
    private static final float CARD_H = Constants.CARD_HEIGHT * 0.7f;
    private static final int COLS = 5;
    private static final float PAD_X = 20f;
    private static final float PAD_Y = 20f;

    public PileViewerOverlay() {
        setSize(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.02f, 0.02f, 0.05f, 0.92f));
        pm.fill();
        bgTex = new Texture(pm);
        pm.setColor(new Color(0.1f, 0.1f, 0.15f, 0.95f));
        pm.fill();
        headerBgTex = new Texture(pm);
        pm.dispose();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(1.8f);
        titleFont.setColor(Color.WHITE);

        countFont = new BitmapFont();
        countFont.getData().setScale(1.0f);
        countFont.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));

        setVisible(false);
        setTouchable(Touchable.enabled);

        // Click anywhere on the overlay background to close
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                hide();
                return true;
            }
        });
    }

    /**
     * Show the overlay with the given list of cards and a title.
     */
    public void show(List<AbstractCard> cards, String title) {
        this.title = title;
        this.totalCards = cards.size();

        // Clear previous card actors
        for (CardActor ca : cardActors) {
            removeActor(ca);
            ca.dispose();
        }
        cardActors.clear();

        // Create card actors in a grid
        float totalGridW = COLS * (CARD_W + PAD_X) - PAD_X;
        float startX = (Constants.VIEWPORT_WIDTH - totalGridW) / 2f;
        float startY = Constants.VIEWPORT_HEIGHT - 100f; // below header

        for (int i = 0; i < cards.size(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            float cx = startX + col * (CARD_W + PAD_X);
            float cy = startY - (row + 1) * (CARD_H + PAD_Y);

            CardActor ca = new CardActor(cards.get(i), (CardActor.OnClickCallback) null);
            ca.setSize(CARD_W, CARD_H);
            ca.setPosition(cx, cy);
            ca.targetPos.set(cx, cy);
            ca.targetScale = 1f;
            ca.targetRot = 0f;
            addActor(ca);
            cardActors.add(ca);
        }

        setVisible(true);
        toFront();
    }

    public void hide() {
        setVisible(false);
        for (CardActor ca : cardActors) {
            removeActor(ca);
            ca.dispose();
        }
        cardActors.clear();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!isVisible()) return;

        // Dark background
        batch.setColor(1, 1, 1, parentAlpha);
        batch.draw(bgTex, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        // Header bar
        batch.draw(headerBgTex, 0, Constants.VIEWPORT_HEIGHT - 70, Constants.VIEWPORT_WIDTH, 70);

        // Title
        titleFont.setColor(Color.WHITE);
        titleFont.draw(batch, title, 40, Constants.VIEWPORT_HEIGHT - 18);

        // Card count
        countFont.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        countFont.draw(batch, totalCards + " cards", Constants.VIEWPORT_WIDTH - 200, Constants.VIEWPORT_HEIGHT - 30);

        // Close hint
        countFont.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        countFont.draw(batch, "Click to close", Constants.VIEWPORT_WIDTH / 2f - 40, 30);

        batch.setColor(1, 1, 1, 1);

        // Draw child card actors
        super.draw(batch, parentAlpha);
    }

    public void disposeResources() {
        for (CardActor ca : cardActors) ca.dispose();
        cardActors.clear();
        bgTex.dispose();
        headerBgTex.dispose();
        titleFont.dispose();
        countFont.dispose();
    }
}
