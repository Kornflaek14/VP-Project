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
import com.cardgame.logic.relics.AbstractRelic;
import com.cardgame.logic.RunManager;
import com.cardgame.utils.Constants;

import java.util.List;
import java.util.Random;

public class TreasureScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private BitmapFont font;
    private BitmapFont titleFont;
    
    private Texture chestClosedTex;
    private Texture chestOpenTex;
    private Texture relicTex;
    
    private boolean opened = false;

    public TreasureScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        try {
            chestClosedTex = new Texture(Gdx.files.internal("IMAGES/play/chest.png"));
            chestOpenTex = new Texture(Gdx.files.internal("IMAGES/play/chestOpened.png"));
        } catch (Exception e) {}

        font = new BitmapFont();
        font.getData().setScale(1.2f);
        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.5f);
        titleFont.setColor(Color.GOLD);

        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label("TREASURE", new Label.LabelStyle(titleFont, titleFont.getColor()));
        root.add(title).padBottom(40).row();

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = Color.YELLOW;

        TextButton openBtn = new TextButton("OPEN CHEST", btnStyle);
        
        final Label rewardLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
        
        TextButton leaveBtn = new TextButton("LEAVE", btnStyle);
        leaveBtn.setVisible(false);
        leaveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MapScreen(game));
            }
        });

        openBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!opened) {
                    opened = true;
                    openBtn.setVisible(false);
                    leaveBtn.setVisible(true);

                    List<AbstractRelic> allRelics = game.getAllRelics();
                    if (!allRelics.isEmpty()) {
                        AbstractRelic reward = allRelics.get(new Random().nextInt(allRelics.size()));
                        RunManager.getInstance().addRelic(reward);
                        RunManager.getInstance().addGold(50);
                        
                        rewardLabel.setText("You found: " + reward.name + " and 50 Gold!");
                        
                        try {
                            if (reward.imagePath != null && !reward.imagePath.isEmpty()) {
                                relicTex = new Texture(Gdx.files.internal(reward.imagePath));
                            }
                        } catch (Exception e) {}
                    }
                }
            }
        });

        root.add(openBtn).size(200, 60).padBottom(20).row();
        root.add(rewardLabel).padBottom(20).row();
        root.add(leaveBtn).size(200, 60).row();

        stage.addActor(root);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Batch batch = stage.getBatch();
        batch.begin();
        Texture tex = opened ? chestOpenTex : chestClosedTex;
        if (tex != null) {
            float w = 256;
            float h = 256;
            batch.draw(tex, Constants.VIEWPORT_WIDTH / 2f - w/2f, Constants.VIEWPORT_HEIGHT / 2f + 50, w, h);
        }
        if (opened && relicTex != null) {
            batch.draw(relicTex, Constants.VIEWPORT_WIDTH / 2f - 32, Constants.VIEWPORT_HEIGHT / 2f + 180, 64, 64);
        }
        batch.end();

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
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (chestClosedTex != null) chestClosedTex.dispose();
        if (chestOpenTex != null) chestOpenTex.dispose();
        if (relicTex != null) relicTex.dispose();
    }
}
