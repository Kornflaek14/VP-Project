package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.ui.UiTheme;
import com.cardgame.utils.Constants;

public class SettingsScreen implements Screen {

    private final CardBattlerGame game;

    private Stage      stage;
    private Texture    bgTexture;
    private BitmapFont titleFont;
    private BitmapFont font;
    private boolean leaving;

    public SettingsScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        leaving = false;
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        try {
            bgTexture = new Texture(Gdx.files.internal("IMAGES/MainMenuBackground.jpg"));
        } catch (Exception e) {
            Gdx.app.error("Settings", "Missing background");
        }

        titleFont = UiTheme.font(40f);
        titleFont.setColor(new Color(0.88f, 0.87f, 0.85f, 1f));

        font = UiTheme.font(24f);

        buildUI();
    }

    private void buildUI() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, titleFont.getColor());
        Label title = new Label("SETTINGS", titleStyle);

        Label.LabelStyle infoStyle = new Label.LabelStyle(font, Color.LIGHT_GRAY);
        Label info = new Label("Display changes apply immediately.", infoStyle);

        TextButton.TextButtonStyle btnStyle = UiTheme.button(font);
        btnStyle.fontColor     = Color.WHITE;

        Preferences preferences = Gdx.app.getPreferences("Locura");
        TextButton vsyncBtn = new TextButton("VSYNC: "
                + (preferences.getBoolean("vsync", true) ? "ON" : "OFF"), btnStyle);
        vsyncBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean enabled = !preferences.getBoolean("vsync", true);
                Gdx.graphics.setVSync(enabled);
                preferences.putBoolean("vsync", enabled).flush();
                vsyncBtn.setText("VSYNC: " + (enabled ? "ON" : "OFF"));
            }
        });

        TextButton displayBtn = new TextButton("DISPLAY: "
                + (Gdx.graphics.isFullscreen() ? "FULLSCREEN" : "WINDOWED"), btnStyle);
        displayBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Window changes can trigger resize; apply after input dispatch.
                Gdx.app.postRunnable(() -> {
                    if (leaving) return;
                    boolean changed = Gdx.graphics.isFullscreen()
                            ? Gdx.graphics.setWindowedMode(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)
                            : Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                    displayBtn.setText("DISPLAY: " + (Gdx.graphics.isFullscreen() ? "FULLSCREEN" : "WINDOWED"));
                    info.setText(changed ? "Display changes apply immediately." : "This display mode is unavailable.");
                });
            }
        });

        TextButton backBtn = new TextButton("BACK", btnStyle);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                returnToMenu();
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.center();

        root.add(title).padBottom(60).row();
        root.add(info).padBottom(35).row();
        root.add(displayBtn).size(480, 65).padBottom(15).row();
        root.add(vsyncBtn).size(480, 65).padBottom(35).row();
        root.add(backBtn).size(260, 60).row();

        stage.addActor(root);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    returnToMenu();
                    return true;
                }
                return false;
            }
        });
    }

    private void returnToMenu() {
        if (leaving) return;
        leaving = true;
        Gdx.app.postRunnable(() -> game.setScreen(new MainMenuScreen(game)));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getViewport().apply();

        if (bgTexture != null) {
            Batch batch = stage.getBatch();
            batch.setProjectionMatrix(stage.getCamera().combined);
            batch.begin();
            float scale = Math.min((float) Constants.VIEWPORT_WIDTH / bgTexture.getWidth(),
                    (float) Constants.VIEWPORT_HEIGHT / bgTexture.getHeight());
            float width = bgTexture.getWidth() * scale;
            float height = bgTexture.getHeight() * scale;
            batch.setColor(0.22f, 0.22f, 0.22f, 1f);
            batch.draw(bgTexture, (Constants.VIEWPORT_WIDTH - width) / 2f,
                    (Constants.VIEWPORT_HEIGHT - height) / 2f, width, height);
            batch.setColor(Color.WHITE);
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
        leaving = true;
        if (stage != null) {
            if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null);
            stage.dispose();
        }
        if (bgTexture != null) { bgTexture.dispose(); bgTexture = null; }
        if (titleFont != null) { titleFont.dispose(); titleFont = null; }
        if (font != null) { font.dispose(); font = null; }
        stage = null;
    }
}
