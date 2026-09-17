package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.cardgame.logic.RunManager;
import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.logic.potions.AbstractPotion;
import com.cardgame.ui.CardActor;
import com.cardgame.ui.UiTheme;
import com.cardgame.ui.GameArt;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RewardScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private Texture bgTexture;
    private BitmapFont font;
    private final List<CardActor> cardActors = new ArrayList<>();
    
    private int goldReward;
    private AbstractPotion potionReward;
    private List<AbstractCard> cardRewards = new ArrayList<>();
    
    private boolean goldClaimed = false;
    private boolean potionClaimed = false;
    private boolean cardClaimed = false;

    private Table rewardsTable;
    private Table cardsTable;
    
    public RewardScreen(CardBattlerGame game) {
        this.game = game;
        generateRewards();
    }
    
    private void generateRewards() {
        Random rand = new Random();
        // 10-25 Gold
        goldReward = 10 + rand.nextInt(16);
        
        // 40% chance for a potion
        if (rand.nextInt(100) < 40) {
            List<AbstractPotion> allPots = game.getAllPotions();
            if (!allPots.isEmpty()) {
                potionReward = allPots.get(rand.nextInt(allPots.size())).makeCopy();
            }
        }
        
        // 3 Random Cards
        List<AbstractCard> allCards = new ArrayList<>(game.getAllCards());
        Collections.shuffle(allCards);
        for(int i = 0; i < 3 && i < allCards.size(); i++) {
            cardRewards.add(allCards.get(i).makeCopy());
        }
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        try { bgTexture = new Texture(Gdx.files.internal(GameArt.BATTLE)); } catch (Exception e) {}
        
        font = UiTheme.font(20f);
        
        buildUI();
        
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);
    }
    
    private void buildUI() {
        TextButton.TextButtonStyle btnStyle = UiTheme.button(font);
        btnStyle.fontColor = Color.WHITE;
        btnStyle.disabledFontColor = Color.GRAY;
        
        Label.LabelStyle lblStyle = new Label.LabelStyle(font, Color.WHITE);
        
        Label titleLabel = new Label("VICTORY! Choose your rewards:", lblStyle);
        titleLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        titleLabel.setBounds(80f, Constants.VIEWPORT_HEIGHT - 110f, Constants.VIEWPORT_WIDTH - 160f, 50f);
        stage.addActor(titleLabel);
        
        rewardsTable = new Table();
        rewardsTable.setPosition(Constants.VIEWPORT_WIDTH / 2f, Constants.VIEWPORT_HEIGHT / 2f + 100f);
        stage.addActor(rewardsTable);
        
        cardsTable = new Table();
        cardsTable.setPosition(Constants.VIEWPORT_WIDTH / 2f, Constants.VIEWPORT_HEIGHT / 2f - 150f);
        stage.addActor(cardsTable);
        
        refreshRewardsTable(btnStyle);
        
        TextButton proceedBtn = new TextButton("PROCEED", btnStyle);
        proceedBtn.setPosition(Constants.VIEWPORT_WIDTH - 200f, 50f);
        proceedBtn.setSize(170f, 54f);
        proceedBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MapScreen(game));
            }
        });
        stage.addActor(proceedBtn);
    }
    
    private void refreshRewardsTable(TextButton.TextButtonStyle btnStyle) {
        rewardsTable.clearChildren();
        
        if (!goldClaimed) {
            TextButton goldBtn = new TextButton(goldReward + " Gold", btnStyle);
            goldBtn.setName("claim-gold");
            goldBtn.clearChildren();
            goldBtn.add(GameArt.image(GameArt.GOLD)).size(38f).padRight(18f);
            goldBtn.add(goldBtn.getLabel());
            goldBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (goldClaimed) return;
                    RunManager.getInstance().addGold(goldReward);
                    goldClaimed = true;
                    refreshRewardsTable(btnStyle);
                }
            });
            rewardsTable.add(goldBtn).minWidth(330f).height(66f).pad(10).row();
        }
        
        if (potionReward != null && !potionClaimed) {
            TextButton potBtn = new TextButton("Potion: " + potionReward.name, btnStyle);
            potBtn.clearChildren();
            potBtn.add(GameArt.image(potionReward.imagePath)).size(36f).padRight(16f);
            potBtn.add(potBtn.getLabel());
            potBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (RunManager.getInstance().addPotion(potionReward)) {
                        potionClaimed = true;
                        refreshRewardsTable(btnStyle);
                    }
                }
            });
            rewardsTable.add(potBtn).pad(10).row();
        }
        
        if (!cardClaimed) {
            TextButton cardBtn = new TextButton("Add a Card to your Deck", btnStyle);
            cardBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    showCardChoices(btnStyle);
                }
            });
            rewardsTable.add(cardBtn).pad(10).row();
        }
    }
    
    private void showCardChoices(TextButton.TextButtonStyle btnStyle) {
        cardsTable.clearChildren();
        rewardsTable.clearChildren(); // Hide other rewards while choosing
        
        for (AbstractCard c : cardRewards) {
            CardActor ca = new CardActor(c, new CardActor.OnClickCallback() {
                @Override
                public void onClick(CardActor actor) {
                    RunManager.getInstance().addCardToDeck(c);
                    cardClaimed = true;
                    cardsTable.clearChildren();
                    refreshRewardsTable(btnStyle);
                }
            });
            ca.isUiElement = true;
            cardActors.add(ca);
            // We need to set a reasonable size since CardActor relies on layout
            ca.setSize(180f, 250f);
            cardsTable.add(ca).size(180f, 250f).pad(20);
        }
        
        cardsTable.row();
        TextButton skipBtn = new TextButton("Skip", btnStyle);
        skipBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                cardClaimed = true;
                cardsTable.clearChildren();
                refreshRewardsTable(btnStyle);
            }
        });
        cardsTable.add(skipBtn).colspan(cardRewards.size()).padTop(20);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getViewport().apply();
        stage.getBatch().setProjectionMatrix(stage.getCamera().combined);

        if (bgTexture != null) {
            stage.getBatch().begin();
            stage.getBatch().setColor(0.28f, 0.28f, 0.32f, 1f);
            GameArt.cover(stage.getBatch(), bgTexture, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
            stage.getBatch().setColor(Color.WHITE);
            stage.getBatch().end();
        }

        stage.act(delta);
        stage.draw();
        
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }
    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (font != null) font.dispose();
        for (CardActor actor : cardActors) actor.dispose();
        cardActors.clear();
    }
}
