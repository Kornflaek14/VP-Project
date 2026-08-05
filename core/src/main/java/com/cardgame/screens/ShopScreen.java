package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.cardgame.data.PotionData;
import com.cardgame.data.RelicData;
import com.cardgame.logic.RunManager;
import com.cardgame.ui.CardActor;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ShopScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private BitmapFont font;
    private BitmapFont titleFont;
    
    private final List<CardActor> cardActors = new ArrayList<>();
    private Label goldLabel;

    public ShopScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        font = new BitmapFont();
        font.getData().setScale(1.0f);
        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.0f);
        titleFont.setColor(Color.GOLD);

        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(20);

        Label title = new Label("MERCHANT SHOP", new Label.LabelStyle(titleFont, titleFont.getColor()));
        root.add(title).colspan(2).padBottom(10).row();
        
        goldLabel = new Label("Gold: " + RunManager.getInstance().getGold(), new Label.LabelStyle(titleFont, Color.YELLOW));
        root.add(goldLabel).colspan(2).padBottom(30).row();

        // 1. Cards for Sale (e.g. 5 random cards)
        Table cardsTable = new Table();
        RunManager rm = RunManager.getInstance();
        String charName = rm.getSelectedCharacter().name();
        List<CardData> pool = game.getCardsForCharacter(charName);
        if (pool.isEmpty()) pool = game.getAllCards();
        List<CardData> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = Color.YELLOW;

        for (int i = 0; i < 5 && i < shuffled.size(); i++) {
            CardData card = shuffled.get(i);
            int price = 45 + new Random().nextInt(20);
            
            Table itemTable = new Table();
            CardActor ca = new CardActor(card, null); // no click on actor itself
            ca.setSize(Constants.CARD_WIDTH * 0.8f, Constants.CARD_HEIGHT * 0.8f);
            cardActors.add(ca);
            itemTable.add(ca).size(Constants.CARD_WIDTH * 0.8f, Constants.CARD_HEIGHT * 0.8f).padBottom(5).row();
            
            TextButton buyBtn = new TextButton(price + " Gold", btnStyle);
            buyBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (rm.spendGold(price)) {
                        rm.addCardToDeck(card);
                        updateGold();
                        buyBtn.setDisabled(true);
                        buyBtn.setText("SOLD");
                    }
                }
            });
            itemTable.add(buyBtn).size(120, 40);
            cardsTable.add(itemTable).pad(10);
        }
        root.add(cardsTable).colspan(2).padBottom(30).row();

        // 2. Relics & Potions
        Table itemsTable = new Table();
        
        // Relic
        List<RelicData> relics = new ArrayList<>(game.getAllRelics());
        Collections.shuffle(relics);
        if (!relics.isEmpty()) {
            RelicData relic = relics.get(0);
            int price = 150 + new Random().nextInt(50);
            TextButton buyRelicBtn = new TextButton("Relic: " + relic.name() + "\n" + price + " Gold", btnStyle);
            buyRelicBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (rm.spendGold(price)) {
                        rm.addRelic(relic);
                        updateGold();
                        buyRelicBtn.setDisabled(true);
                        buyRelicBtn.setText("SOLD");
                    }
                }
            });
            itemsTable.add(buyRelicBtn).size(250, 80).pad(10);
        }

        // Potion
        List<PotionData> potions = new ArrayList<>(game.getAllPotions());
        Collections.shuffle(potions);
        if (!potions.isEmpty()) {
            PotionData potion = potions.get(0);
            int price = 50 + new Random().nextInt(20);
            TextButton buyPotionBtn = new TextButton("Potion: " + potion.name() + "\n" + price + " Gold", btnStyle);
            buyPotionBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (rm.addPotion(potion)) {
                        if (rm.spendGold(price)) {
                            updateGold();
                            buyPotionBtn.setDisabled(true);
                            buyPotionBtn.setText("SOLD");
                        } else {
                            rm.getPotions().remove(potion); // revert if can't afford
                        }
                    }
                }
            });
            itemsTable.add(buyPotionBtn).size(250, 80).pad(10);
        }
        
        root.add(itemsTable).colspan(2).padBottom(40).row();

        // Leave Button
        TextButton leaveBtn = new TextButton("LEAVE SHOP", btnStyle);
        leaveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MapScreen(game));
            }
        });
        root.add(leaveBtn).size(250, 60).colspan(2).row();

        stage.addActor(root);
    }
    
    private void updateGold() {
        goldLabel.setText("Gold: " + RunManager.getInstance().getGold());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
        for (CardActor ca : cardActors) ca.dispose();
        cardActors.clear();
    }
}
