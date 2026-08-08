package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
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
import com.cardgame.logic.relics.AbstractRelic;
import com.cardgame.ui.CardActor;
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
    
    private int goldReward;
    private AbstractPotion potionReward;
    private AbstractRelic relicReward;
    private List<AbstractCard> cardRewards = new ArrayList<>();
    
    private boolean goldClaimed = false;
    private boolean potionClaimed = false;
    private boolean relicClaimed = false;
    private boolean cardClaimed = false;

    private Table rewardsTable;
    private Table cardsTable;
    
    public RewardScreen(CardBattlerGame game) {
        this.game = game;
        generateRewards();
    }
    
    private void generateRewards() {
        Random rand = new Random();
        RunManager rm = RunManager.getInstance();
        RunManager.MapNodeData lastNode = rm.getNodeById(rm.getLastVisitedNodeId());
        if (lastNode != null && "ELITE".equals(lastNode.type)) {
            List<AbstractRelic> allRelics = game.getAllRelics();
            List<AbstractRelic> ownedRelics = rm.getRelics();
            List<AbstractRelic> available = new ArrayList<>();
            for (AbstractRelic r : allRelics) {
                boolean owns = false;
                for (AbstractRelic owned : ownedRelics) {
                    if (owned.getClass().equals(r.getClass())) {
                        owns = true;
                        break;
                    }
                }
                if (!owns) {
                    available.add(r);
                }
            }
            if (!available.isEmpty()) {
                relicReward = available.get(rand.nextInt(available.size()));
            }
        }

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
        try { bgTexture = new Texture(Gdx.files.internal("IMAGES/play/playBackground.jpg")); } catch (Exception e) {}
        
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        
        buildUI();
        
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);
    }
    
    private void buildUI() {
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = Color.YELLOW;
        btnStyle.disabledFontColor = Color.GRAY;
        
        Label.LabelStyle lblStyle = new Label.LabelStyle(font, Color.WHITE);
        
        Label titleLabel = new Label("VICTORY! Choose your rewards:", lblStyle);
        titleLabel.setPosition(Constants.VIEWPORT_WIDTH / 2f - 200f, Constants.VIEWPORT_HEIGHT - 100f);
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
            goldBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    RunManager.getInstance().addGold(goldReward);
                    goldClaimed = true;
                    refreshRewardsTable(btnStyle);
                }
            });
            rewardsTable.add(goldBtn).pad(10).row();
        }
        
        if (relicReward != null && !relicClaimed) {
            TextButton relicBtn = new TextButton("Relic: " + relicReward.name, btnStyle);
            relicBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    RunManager.getInstance().addRelic(relicReward);
                    relicClaimed = true;
                    refreshRewardsTable(btnStyle);
                }
            });
            rewardsTable.add(relicBtn).pad(10).row();
        }
        
        if (potionReward != null && !potionClaimed) {
            TextButton potBtn = new TextButton("Potion: " + potionReward.name, btnStyle);
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
                    showCardChoices();
                }
            });
            rewardsTable.add(cardBtn).pad(10).row();
        }
    }
    
    private void showCardChoices() {
        cardsTable.clearChildren();
        for (AbstractCard c : cardRewards) {
            CardActor ca = new CardActor(c, new CardActor.OnClickCallback() {
                @Override
                public void onClick(CardActor actor) {
                    RunManager.getInstance().addCardToDeck(c);
                    cardClaimed = true;
                    cardsTable.clearChildren();
                }
            });
            // We need to set a reasonable size since CardActor relies on layout
            ca.setSize(180f, 250f);
            cardsTable.add(ca).size(180f, 250f).pad(20);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (bgTexture != null) {
            stage.getBatch().begin();
            stage.getBatch().draw(bgTexture, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
            stage.getBatch().end();
        }

        stage.act(delta);
        stage.draw();
        
        if (cardClaimed) {
            cardsTable.clearChildren();
            // Nasty hack to clean up UI state inline
            cardClaimed = false;
            cardRewards.clear(); 
            // the button is already hidden by refresh loop logic mostly
        }
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
    }
}
