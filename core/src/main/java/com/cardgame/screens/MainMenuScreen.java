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

/**
 * First screen the player sees. Renders a title and a PLAY button that
 * transitions to {@link BattleScreen}.
 * <p>
 * All rendering classes live in {@code screens/} or {@code ui/}; no game
 * logic is executed here.
 */
public class MainMenuScreen implements Screen {

    private final CardBattlerGame game;

    private Stage   stage;
    private Texture bgTexture;

    // Shared font/style (disposed on hide)
    private BitmapFont titleFont;
    private BitmapFont buttonFont;

    public MainMenuScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        buildBackground();
        buildUI();
    }

    // ── UI construction ────────────────────────────────────────────────────────

    private void buildBackground() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.06f, 0.06f, 0.14f, 1f));
        pm.fill();
        bgTexture = new Texture(pm);
        pm.dispose();
    }

    private void buildUI() {
        // Fonts — fall back to BitmapFont if FreeType is unavailable
        titleFont  = new BitmapFont();
        buttonFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);
        buttonFont.getData().setScale(1.8f);
        titleFont.setColor(new Color(0.96f, 0.84f, 0.38f, 1f)); // gold
        buttonFont.setColor(Color.WHITE);

        // ── Title label ────────────────────────────────────────────────────────
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, titleFont.getColor());
        Label title = new Label("CARD  BATTLER", titleStyle);

        // ── Subtitle ───────────────────────────────────────────────────────────
        BitmapFont subFont = new BitmapFont();
        subFont.getData().setScale(1.1f);
        subFont.setColor(new Color(0.65f, 0.65f, 0.75f, 1f));
        Label.LabelStyle subStyle = new Label.LabelStyle(subFont, subFont.getColor());
        Label subtitle = new Label("1v1 Board-Based Duelling", subStyle);

        // ── Play button ────────────────────────────────────────────────────────
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font       = buttonFont;
        btnStyle.fontColor  = Color.WHITE;
        btnStyle.overFontColor = new Color(0.96f, 0.84f, 0.38f, 1f);

        TextButton playBtn = new TextButton("▶  PLAY", btnStyle);
        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new BattleScreen(game));
            }
        });

        // ── Layout ─────────────────────────────────────────────────────────────
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        root.add(title).padBottom(12).row();
        root.add(subtitle).padBottom(60).row();
        root.add(playBtn).size(260, 60).padBottom(20).row();

        stage.addActor(root);
    }

    // ── Screen lifecycle ───────────────────────────────────────────────────────

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.06f, 0.06f, 0.14f, 1f);
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
