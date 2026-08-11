package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.utils.Constants;

public class MainMenuScreen implements Screen {

    private final CardBattlerGame game;

    private Stage   stage;
    private Texture bgTexture;

    private BitmapFont titleFont;
    private BitmapFont buttonFont;

    public MainMenuScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        try {
            bgTexture = new Texture(Gdx.files.internal("IMAGES/MainMenuBackground.jpg"));
        } catch (Exception e) {
            Gdx.app.error("MainMenu", "Missing MainMenuBackground.jpg");
        }

        buildUI();
    }

    private void buildUI() {
        titleFont  = new BitmapFont();
        buttonFont = new BitmapFont();
        titleFont.getData().setScale(4.5f);
        buttonFont.getData().setScale(1.8f);
        titleFont.setColor(new Color(0.96f, 0.84f, 0.38f, 1f));
        buttonFont.setColor(Color.WHITE);

        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, titleFont.getColor());
        Label title = new Label("CARD BATTLER", titleStyle);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font       = buttonFont;
        btnStyle.fontColor  = Color.WHITE;
        btnStyle.overFontColor = new Color(0.96f, 0.84f, 0.38f, 1f);

        // ── Play button ───────────────────────────────────────────
        TextButton playBtn = new TextButton("PLAY", btnStyle);
        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new CharacterSelectScreen(game));
            }
        });

        // ── Settings button ───────────────────────────────────────
        TextButton settingsBtn = new TextButton("SETTINGS", btnStyle);
        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SettingsScreen(game));
            }
        });

        // ── Exit button ───────────────────────────────────────────
        TextButton exitBtn = new TextButton("EXIT", btnStyle);
        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.center();

        root.add(title).padBottom(80).row();
        root.add(playBtn).size(260, 60).padBottom(20).row();
        root.add(settingsBtn).size(260, 60).padBottom(20).row();
        root.add(exitBtn).size(260, 60).row();

        stage.addActor(root);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (bgTexture != null) {
            Batch batch = stage.getBatch();
            batch.begin();
            batch.draw(bgTexture, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
            batch.end();
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (stage     != null) stage.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        stage = null;
    }
}
