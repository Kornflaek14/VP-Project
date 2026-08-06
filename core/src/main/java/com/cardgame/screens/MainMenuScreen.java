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
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.utils.Constants;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;

public class MainMenuScreen implements Screen {

    private final CardBattlerGame game;

    private Stage   stage;
    private Texture bgTexture;
    
    private Animation<TextureRegion> backgroundAnimation;
    private float stateTime = 0f;
    private final List<Texture> frameTextures = new ArrayList<>();

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

        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < 240; i++) {
            String filename = String.format("IMAGES/menu_frames/frame_%03d.jpg", i);
            if (Gdx.files.internal(filename).exists()) {
                Texture tex = new Texture(Gdx.files.internal(filename));
                frameTextures.add(tex);
                frames.add(new TextureRegion(tex));
            }
        }
        if (frames.size > 0) {
            backgroundAnimation = new Animation<>(1f / 24f, frames, Animation.PlayMode.LOOP);
        }

        buildUI();
    }

    private void buildUI() {
        try {
            Texture startTex = new Texture(Gdx.files.internal("IMAGES/start_game.png"));
            Texture loadTex = new Texture(Gdx.files.internal("IMAGES/load_game.png"));
            Texture optTex = new Texture(Gdx.files.internal("IMAGES/options.png"));
            Texture exitTex = new Texture(Gdx.files.internal("IMAGES/exit.png"));
            
            frameTextures.add(startTex);
            frameTextures.add(loadTex);
            frameTextures.add(optTex);
            frameTextures.add(exitTex);

            Image startBtn = new Image(startTex);
            Image loadBtn = new Image(loadTex);
            Image optBtn = new Image(optTex);
            Image exitBtn = new Image(exitTex);

            // Left side (Left-aligned block, flanking brain)
            startBtn.setPosition(300, 220);
            loadBtn.setPosition(300, 120);
            
            // Right side (Left-aligned block, flanking brain)
            optBtn.setPosition(890, 220);
            exitBtn.setPosition(890, 120);

            Color logoRed = new Color(0.75f, 0.11f, 0.14f, 1f);

            startBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (game.getAllCharacters().isEmpty()) return;
                    com.cardgame.data.CharacterData ch = game.getAllCharacters().get(0);
                    List<com.cardgame.data.CardData> starterDeck = new ArrayList<>();
                    for (com.cardgame.data.CardData c : game.getAllCards()) {
                        if (c.name().equalsIgnoreCase("Frantic Strike")) {
                            for(int i=0; i<5; i++) starterDeck.add(c);
                        } else if (c.name().equalsIgnoreCase("Deny Reality")) {
                            for(int i=0; i<4; i++) starterDeck.add(c);
                        } else if (c.name().equalsIgnoreCase("Skull Crack")) {
                            starterDeck.add(c);
                        }
                    }
                    com.cardgame.logic.RunManager.getInstance().startNewRun(ch, starterDeck);
                    game.setScreen(new MapScreen(game));
                }
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    startBtn.setColor(logoRed);
                }
                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    startBtn.setColor(Color.WHITE);
                }
            });

            loadBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // game.setScreen(new LoadGameScreen(game));
                }
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    loadBtn.setColor(logoRed);
                }
                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    loadBtn.setColor(Color.WHITE);
                }
            });

            optBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // game.setScreen(new OptionsScreen(game));
                }
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    optBtn.setColor(logoRed);
                }
                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    optBtn.setColor(Color.WHITE);
                }
            });

            exitBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.exit();
                }
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    exitBtn.setColor(logoRed);
                }
                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    exitBtn.setColor(Color.WHITE);
                }
            });

            stage.addActor(startBtn);
            stage.addActor(loadBtn);
            stage.addActor(optBtn);
            stage.addActor(exitBtn);

        } catch (Exception e) {
            Gdx.app.error("MainMenu", "Missing button images");
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stateTime += delta;

        Batch batch = stage.getBatch();
        batch.begin();
        if (backgroundAnimation != null) {
            TextureRegion currentFrame = backgroundAnimation.getKeyFrame(stateTime);
            batch.draw(currentFrame, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        } else if (bgTexture != null) {
            batch.draw(bgTexture, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        }
        batch.end();

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
        for (Texture t : frameTextures) t.dispose();
        frameTextures.clear();
        stage = null;
    }
}
