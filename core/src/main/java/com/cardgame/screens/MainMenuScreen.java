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
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

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
        for (int i = 0; i < 1000; i++) {
            String filename = String.format("IMAGES/menu_frames/frame_%03d.jpg", i);
            if (Gdx.files.internal(filename).exists()) {
                Texture tex = new Texture(Gdx.files.internal(filename));
                frameTextures.add(tex);
                frames.add(new TextureRegion(tex));
            } else if (i > 0) {
                break; // Stop loading when we hit a missing frame
            }
        }
        if (frames.size > 0) {
            backgroundAnimation = new Animation<>(1f / 24f, frames, Animation.PlayMode.LOOP);
        }

        buildUI();
    }

    private void buildUI() {
        try {
            Texture spritesheet = new Texture(Gdx.files.internal("IMAGES/menu_spritesheet_clean.png"));
            frameTextures.add(spritesheet);

            int regionWidth = spritesheet.getWidth() / 2;
            int regionHeight = spritesheet.getHeight() / 4;
            TextureRegion[][] regions = TextureRegion.split(spritesheet, regionWidth, regionHeight);

            ImageButton.ImageButtonStyle startStyle = new ImageButton.ImageButtonStyle();
            startStyle.imageUp = new TextureRegionDrawable(regions[0][0]);
            startStyle.imageOver = new TextureRegionDrawable(regions[0][1]);
            ImageButton startBtn = new ImageButton(startStyle);

            ImageButton.ImageButtonStyle loadStyle = new ImageButton.ImageButtonStyle();
            loadStyle.imageUp = new TextureRegionDrawable(regions[1][0]);
            loadStyle.imageOver = new TextureRegionDrawable(regions[1][1]);
            ImageButton loadBtn = new ImageButton(loadStyle);

            ImageButton.ImageButtonStyle optStyle = new ImageButton.ImageButtonStyle();
            optStyle.imageUp = new TextureRegionDrawable(regions[2][0]);
            optStyle.imageOver = new TextureRegionDrawable(regions[2][1]);
            ImageButton optBtn = new ImageButton(optStyle);

            ImageButton.ImageButtonStyle exitStyle = new ImageButton.ImageButtonStyle();
            exitStyle.imageUp = new TextureRegionDrawable(regions[3][0]);
            exitStyle.imageOver = new TextureRegionDrawable(regions[3][1]);
            ImageButton exitBtn = new ImageButton(exitStyle);

            // Scale buttons down by half to fit the screen
            startBtn.setTransform(true);
            startBtn.setScale(0.5f);
            loadBtn.setTransform(true);
            loadBtn.setScale(0.5f);
            optBtn.setTransform(true);
            optBtn.setScale(0.5f);
            exitBtn.setTransform(true);
            exitBtn.setScale(0.5f);

            // Left-aligned vertical layout with proper spacing
            startBtn.setPosition(50, 500);
            loadBtn.setPosition(50, 380);
            optBtn.setPosition(50, 260);
            exitBtn.setPosition(50, 140);

            startBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (game.getAllCharacters().isEmpty()) return;
                    com.cardgame.data.CharacterData ch = game.getAllCharacters().get(0);
                    List<com.cardgame.logic.cards.AbstractCard> starterDeck = new ArrayList<>();
                    for (com.cardgame.logic.cards.AbstractCard c : game.getAllCards()) {
                        if (c.name().equalsIgnoreCase("Scalpel")) {
                            for(int i=0; i<5; i++) starterDeck.add(c.makeCopy());
                        } else if (c.name().equalsIgnoreCase("Cower")) {
                            for(int i=0; i<4; i++) starterDeck.add(c.makeCopy());
                        } else if (c.name().equalsIgnoreCase("Pipe")) {
                            starterDeck.add(c.makeCopy());
                        }
                    }
                    com.cardgame.logic.RunManager.getInstance().startNewRun(ch, starterDeck);
                    game.setScreen(new MapScreen(game));
                }
            });

            loadBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // game.setScreen(new LoadGameScreen(game));
                }
            });

            optBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // game.setScreen(new OptionsScreen(game));
                }
            });

            exitBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.exit();
                }
            });

            stage.addActor(startBtn);
            stage.addActor(loadBtn);
            stage.addActor(optBtn);
            stage.addActor(exitBtn);

        } catch (Exception e) {
            Gdx.app.error("MainMenu", "Missing menu button textures. " + e.getMessage());
            e.printStackTrace();
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
