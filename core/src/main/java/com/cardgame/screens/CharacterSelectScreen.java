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
import com.cardgame.data.CharacterData;
import com.cardgame.logic.RunManager;
import com.cardgame.utils.Constants;

import java.util.List;
import java.util.ArrayList;

public class CharacterSelectScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private Texture bgTexture;
    private BitmapFont font;
    private BitmapFont titleFont;
    
    private final List<Texture> texturesToDispose = new ArrayList<>();

    public CharacterSelectScreen(CardBattlerGame game) {
        this.game = game;
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
        titleFont.getData().setScale(2.0f);
        titleFont.setColor(Color.WHITE);

        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label("CHOOSE YOUR CHARACTER", new Label.LabelStyle(titleFont, Color.WHITE));
        root.add(title).colspan(3).padBottom(50).row();

        Table charsTable = new Table();

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = Color.YELLOW;

        for (CharacterData ch : game.getAllCharacters()) {
            Table charCol = new Table();

            // Portrait
            if (ch.portrait() != null && !ch.portrait().isEmpty()) {
                try {
                    Texture tex = new Texture(Gdx.files.internal(ch.portrait()));
                    texturesToDispose.add(tex);
                    com.badlogic.gdx.scenes.scene2d.ui.Image img = new com.badlogic.gdx.scenes.scene2d.ui.Image(tex);
                    charCol.add(img).size(200, 250).padBottom(10).row();
                } catch (Exception e) {}
            }

            Label nameLabel = new Label(ch.name(), new Label.LabelStyle(font, Color.GOLD));
            charCol.add(nameLabel).padBottom(10).row();

            Label statsLabel = new Label("HP: " + ch.hp() + "\nGold: " + ch.gold() + "\nRelic: " + ch.startingRelicName(), new Label.LabelStyle(font, Color.LIGHT_GRAY));
            statsLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
            charCol.add(statsLabel).padBottom(20).row();

            TextButton selectBtn = new TextButton("SELECT", btnStyle);
            selectBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    List<com.cardgame.data.CardData> starterDeck = new ArrayList<>();
                    if (ch.name().equalsIgnoreCase("Ironclad")) {
                        for (com.cardgame.data.CardData c : game.getAllCards()) {
                            if (c.name().equalsIgnoreCase("Frantic Strike")) {
                                for(int i=0; i<5; i++) starterDeck.add(c);
                            } else if (c.name().equalsIgnoreCase("Deny Reality")) {
                                for(int i=0; i<4; i++) starterDeck.add(c);
                            } else if (c.name().equalsIgnoreCase("Skull Crack")) {
                                starterDeck.add(c);
                            }
                        }
                    } else {
                        starterDeck = game.getCardsForCharacter(ch.name());
                    }
                    RunManager.getInstance().startNewRun(ch, starterDeck);
                    game.setScreen(new MapScreen(game));
                }
            });
            charCol.add(selectBtn).size(150, 50).row();

            charsTable.add(charCol).pad(0, 30, 0, 30);
        }

        root.add(charsTable).row();
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
        for (Texture t : texturesToDispose) t.dispose();
        texturesToDispose.clear();
    }
}
