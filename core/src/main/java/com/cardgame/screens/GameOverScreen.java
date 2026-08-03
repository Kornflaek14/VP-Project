package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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

public class GameOverScreen implements Screen {

    private final CardBattlerGame game;
    private final boolean playerWon;

    private Stage   stage;
    private Texture bgTexture;

    private BitmapFont titleFont;
    private BitmapFont buttonFont;

    public GameOverScreen(CardBattlerGame game, int winnerIndex) {
        this.game = game;
        this.playerWon = (winnerIndex == 0);
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        buildBackground();
        buildUI();
    }

    private void buildBackground() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        if (playerWon) {
            pm.setColor(new Color(0.06f, 0.14f, 0.08f, 1f)); // Dark green
        } else {
            pm.setColor(new Color(0.14f, 0.06f, 0.06f, 1f)); // Dark red
        }
        pm.fill();
        bgTexture = new Texture(pm);
        pm.dispose();
    }

    private void buildUI() {
        titleFont  = new BitmapFont();
        buttonFont = new BitmapFont();
        titleFont.getData().setScale(4.0f);
        buttonFont.getData().setScale(1.8f);
        
        Color titleColor = playerWon ? new Color(0.3f, 0.9f, 0.4f, 1f) : new Color(0.9f, 0.3f, 0.3f, 1f);
        titleFont.setColor(titleColor);
        buttonFont.setColor(Color.WHITE);

        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, titleFont.getColor());
        String titleText = playerWon ? "VICTORY" : "DEFEAT";
        Label title = new Label(titleText, titleStyle);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font       = buttonFont;
        btnStyle.fontColor  = Color.WHITE;
        btnStyle.overFontColor = new Color(0.96f, 0.84f, 0.38f, 1f);

        TextButton backBtn = new TextButton("MAIN MENU", btnStyle);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.center();

        root.add(title).padBottom(80).row();
        root.add(backBtn).size(260, 60).row();

        stage.addActor(root);
    }

    @Override
    public void render(float delta) {
        if (playerWon) {
            Gdx.gl.glClearColor(0.06f, 0.14f, 0.08f, 1f);
        } else {
            Gdx.gl.glClearColor(0.14f, 0.06f, 0.06f, 1f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (stage     != null) stage.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        stage = null;
    }
}
