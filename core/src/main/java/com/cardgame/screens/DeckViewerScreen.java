package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.data.CardData;
import com.cardgame.logic.RunManager;
import com.cardgame.ui.CardActor;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows all cards in the player's current deck in a scrollable grid.
 * Accessible from MapScreen. No card selection — read-only.
 */
public class DeckViewerScreen implements Screen {

    private final CardBattlerGame game;
    private final Screen returnScreen;

    private Stage stage;
    private Texture bgTex;
    private BitmapFont font;
    private BitmapFont titleFont;

    private final List<CardActor> cardActors = new ArrayList<>();

    public DeckViewerScreen(CardBattlerGame game, Screen returnScreen) {
        this.game = game;
        this.returnScreen = returnScreen;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        // Dark gradient background
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.05f, 0.05f, 0.12f, 1f));
        pm.fill();
        bgTex = new Texture(pm);
        pm.dispose();

        font = new BitmapFont();
        font.getData().setScale(1.2f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.2f);
        titleFont.setColor(new Color(0.8f, 0.7f, 1f, 1f));

        buildUI();
    }

    private void buildUI() {
        RunManager rm = RunManager.getInstance();
        List<CardData> deck = rm.getDeck();

        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(20);

        // Title
        Label title = new Label("YOUR DECK  (" + deck.size() + " cards)",
            new Label.LabelStyle(titleFont, titleFont.getColor()));
        root.add(title).padBottom(20).row();

        // ── Card grid ─────────────────────────────────────────
        Table cardGrid = new Table();
        cardGrid.top().left().pad(10);

        int cols = 5;
        float cardW = Constants.CARD_WIDTH  * 0.85f;
        float cardH = Constants.CARD_HEIGHT * 0.85f;

        for (int i = 0; i < deck.size(); i++) {
            CardData cd = deck.get(i);
            CardActor ca = new CardActor(cd, null);
            ca.setSize(cardW, cardH);
            cardActors.add(ca);

            cardGrid.add(ca).size(cardW, cardH).pad(10);
            if ((i + 1) % cols == 0) cardGrid.row();
        }

        // Wrap grid in scroll pane
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        ScrollPane scroll = new ScrollPane(cardGrid, scrollStyle);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        root.add(scroll).expand().fill().padBottom(20).row();

        // Back button
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = Color.YELLOW;

        TextButton backBtn = new TextButton("← BACK TO MAP", btnStyle);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(returnScreen);
            }
        });
        root.add(backBtn).size(240, 55).padBottom(20);

        stage.addActor(root);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (bgTex != null) bgTex.dispose();
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        for (CardActor ca : cardActors) ca.dispose();
        cardActors.clear();
    }
}
