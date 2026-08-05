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
import com.cardgame.data.CardData;
import com.cardgame.logic.RunManager;
import com.cardgame.ui.CardActor;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameOverScreen implements Screen {

    private final CardBattlerGame game;
    private final boolean playerWon;

    private Stage stage;
    private Texture bgTexture;
    private BitmapFont font;
    private BitmapFont titleFont;

    private final List<CardActor> cardActors = new ArrayList<>();

    public GameOverScreen(CardBattlerGame game, int winnerIndex) {
        this.game = game;
        this.playerWon = (winnerIndex == 0);
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        try {
            bgTexture = new Texture(Gdx.files.internal("IMAGES/MainMenuBackground.jpg"));
        } catch (Exception e) {}

        font = new BitmapFont();
        font.getData().setScale(1.2f);
        
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.0f);

        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = Color.YELLOW;

        if (playerWon) {
            titleFont.setColor(Color.GREEN);
            Label title = new Label("VICTORY", new Label.LabelStyle(titleFont, titleFont.getColor()));
            root.add(title).padBottom(40).row();

            RunManager rm = RunManager.getInstance();
            if (rm.getCurrentNodeIndex() >= rm.getMaxNodes()) {
                Label winLabel = new Label("YOU WIN! RUN COMPLETE!", new Label.LabelStyle(font, Color.GOLD));
                root.add(winLabel).padBottom(40).row();

                TextButton menuBtn = new TextButton("RESTART RUN", btnStyle);
                menuBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        game.startNewGame();
                    }
                });
                root.add(menuBtn).size(200, 50).row();
            } else {
                Label rewardLabel = new Label("Choose a card reward:", new Label.LabelStyle(font, Color.WHITE));
                root.add(rewardLabel).padBottom(20).row();

                // Generate 3 random cards for the character
                String charName = rm.getSelectedCharacter().name();
                List<CardData> pool = game.getCardsForCharacter(charName);
                if (pool.isEmpty()) pool = game.getAllCards(); // fallback
                
                List<CardData> shuffled = new ArrayList<>(pool);
                Collections.shuffle(shuffled);

                Table cardTable = new Table();
                for (int i = 0; i < 3 && i < shuffled.size(); i++) {
                    CardData rewardCard = shuffled.get(i);
                    CardActor ca = new CardActor(rewardCard, new CardActor.OnClickCallback() {
                        @Override
                        public void onClick(CardActor actor) {
                            rm.addCardToDeck(rewardCard);
                            // Award gold
                            rm.addGold(30);
                            game.setScreen(new MapScreen(game));
                        }
                    });
                    ca.setSize(Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
                    cardActors.add(ca);
                    cardTable.add(ca).size(Constants.CARD_WIDTH, Constants.CARD_HEIGHT).pad(20);
                }
                root.add(cardTable).padBottom(40).row();

                TextButton skipBtn = new TextButton("SKIP REWARD", btnStyle);
                skipBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        rm.addGold(30);
                        game.setScreen(new MapScreen(game));
                    }
                });
                root.add(skipBtn).size(200, 50).row();
            }

        } else {
            titleFont.setColor(Color.RED);
            Label title = new Label("DEFEAT", new Label.LabelStyle(titleFont, titleFont.getColor()));
            root.add(title).padBottom(40).row();

            TextButton menuBtn = new TextButton("RESTART RUN", btnStyle);
            menuBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.startNewGame();
                }
            });
            root.add(menuBtn).size(200, 50).row();
        }

        stage.addActor(root);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1f);
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

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        for (CardActor ca : cardActors) ca.dispose();
        cardActors.clear();
    }
}
