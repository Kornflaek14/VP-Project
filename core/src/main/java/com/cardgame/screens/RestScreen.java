package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
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
import java.util.List;

/**
 * Rest site with four options:
 *   REST   – heal 30% max HP
 *   SMITH  – upgrade one card from deck (+3 damage or +3 block)
 *   REMOVE – remove one card from deck
 *   PROCEED – leave without doing anything
 */
public class RestScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private Texture bgTexture;
    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont smallFont;

    // Card picker overlay (shared for SMITH and REMOVE modes)
    private Group cardPickerOverlay;
    private final List<CardActor> overlayCardActors = new ArrayList<>();
    private boolean pickMode = false; // true = active overlay

    public RestScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        try {
            bgTexture = new Texture(Gdx.files.internal("IMAGES/play/restBg.jpg"));
        } catch (Exception e) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(new Color(0.12f, 0.07f, 0.05f, 1f));
            pm.fill();
            bgTexture = new Texture(pm);
            pm.dispose();
        }

        font = new BitmapFont();
        font.getData().setScale(1.2f);

        smallFont = new BitmapFont();
        smallFont.getData().setScale(0.95f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.5f);
        titleFont.setColor(Color.ORANGE);

        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label("REST SITE", new Label.LabelStyle(titleFont, titleFont.getColor()));
        root.add(title).padBottom(50).colspan(2).row();

        RunManager rm = RunManager.getInstance();
        int healAmount = (int)(rm.getMaxHp() * 0.3f);

        TextButton.TextButtonStyle btnStyle = makeBtnStyle(Color.WHITE, Color.YELLOW);

        // ── REST ─────────────────────────────────────────────
        TextButton healBtn = new TextButton("  REST\n(Heal " + healAmount + " HP)", btnStyle);
        healBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rm.heal(healAmount);
                game.setScreen(new MapScreen(game));
            }
        });

        // ── SMITH (Upgrade) ──────────────────────────────────
        TextButton smithBtn = new TextButton("  SMITH\n(Upgrade a card)", makeBtnStyle(new Color(0.4f, 0.9f, 0.4f, 1f), Color.WHITE));
        smithBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                openCardPicker(false); // false = upgrade mode
            }
        });

        // ── REMOVE ───────────────────────────────────────────
        TextButton removeBtn = new TextButton("  REMOVE\n(Remove a card)", makeBtnStyle(new Color(0.9f, 0.4f, 0.4f, 1f), Color.WHITE));
        removeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                openCardPicker(true); // true = remove mode
            }
        });

        // ── PROCEED ──────────────────────────────────────────
        TextButton leaveBtn = new TextButton("  PROCEED", btnStyle);
        leaveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MapScreen(game));
            }
        });

        root.add(healBtn).size(220, 90).pad(15);
        root.add(smithBtn).size(220, 90).pad(15).row();
        root.add(removeBtn).size(220, 90).pad(15);
        root.add(leaveBtn).size(220, 90).pad(15).row();

        stage.addActor(root);
    }

    /**
     * Opens an overlay showing all deck cards.
     * @param removeMode true = clicking a card removes it; false = clicking upgrades it
     */
    private void openCardPicker(boolean removeMode) {
        if (pickMode) return;
        pickMode = true;

        RunManager rm = RunManager.getInstance();
        List<CardData> deck = rm.getDeck();

        // Dark backdrop
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0f, 0f, 0f, 0.82f));
        pm.fill();
        Texture backdropTex = new Texture(pm);
        pm.dispose();

        cardPickerOverlay = new Group() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.setColor(1, 1, 1, 0.85f * parentAlpha);
                batch.draw(backdropTex, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
                super.draw(batch, parentAlpha);
            }
        };
        cardPickerOverlay.setSize(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        // Title
        BitmapFont overlayFont = new BitmapFont();
        overlayFont.getData().setScale(1.8f);
        overlayFont.setColor(Color.WHITE);
        String prompt = removeMode ? "Choose a card to REMOVE from your deck" : "Choose a card to UPGRADE (+3 dmg/blk)";
        Label promptLabel = new Label(prompt, new Label.LabelStyle(overlayFont, overlayFont.getColor()));
        promptLabel.setPosition(Constants.VIEWPORT_WIDTH / 2f - 280, Constants.VIEWPORT_HEIGHT - 80);
        cardPickerOverlay.addActor(promptLabel);

        // Card grid in a scroll pane
        Table cardGrid = new Table();
        cardGrid.top().left().pad(10);
        int cols = 5;
        float cardW = Constants.CARD_WIDTH  * 0.8f;
        float cardH = Constants.CARD_HEIGHT * 0.8f;

        for (int i = 0; i < deck.size(); i++) {
            CardData cd = deck.get(i);
            final CardData selectedCard = cd;

            CardActor ca = new CardActor(cd, new CardActor.OnClickCallback() {
                @Override
                public void onClick(CardActor actor) {
                    if (removeMode) {
                        rm.removeCardFromDeck(selectedCard);
                    } else {
                        rm.removeCardFromDeck(selectedCard);
                        rm.addCardToDeck(selectedCard.withUpgrade());
                    }
                    closeCardPicker();
                    game.setScreen(new MapScreen(game));
                }
            });
            ca.setSize(cardW, cardH);
            overlayCardActors.add(ca);
            cardGrid.add(ca).size(cardW, cardH).pad(8);
            if ((i + 1) % cols == 0) cardGrid.row();
        }

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        ScrollPane scroll = new ScrollPane(cardGrid, scrollStyle);
        scroll.setScrollingDisabled(true, false);
        scroll.setSize(Constants.VIEWPORT_WIDTH - 80, Constants.VIEWPORT_HEIGHT - 160);
        scroll.setPosition(40, 80);
        cardPickerOverlay.addActor(scroll);

        // Cancel button
        TextButton.TextButtonStyle cancelStyle = makeBtnStyle(Color.LIGHT_GRAY, Color.WHITE);
        TextButton cancelBtn = new TextButton("CANCEL", cancelStyle);
        cancelBtn.setSize(160, 50);
        cancelBtn.setPosition(Constants.VIEWPORT_WIDTH / 2f - 80, 20);
        cancelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                closeCardPicker();
            }
        });
        cardPickerOverlay.addActor(cancelBtn);

        stage.addActor(cardPickerOverlay);
    }

    private void closeCardPicker() {
        if (cardPickerOverlay != null) {
            cardPickerOverlay.remove();
            cardPickerOverlay = null;
        }
        for (CardActor ca : overlayCardActors) ca.dispose();
        overlayCardActors.clear();
        pickMode = false;
    }

    private TextButton.TextButtonStyle makeBtnStyle(Color color, Color hover) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
        s.font = font;
        s.fontColor = color;
        s.overFontColor = hover;
        return s;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.05f, 0.05f, 1f);
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
        closeCardPicker();
        if (stage != null) stage.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (smallFont != null) smallFont.dispose();
    }
}
