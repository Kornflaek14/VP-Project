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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.logic.RunManager;
import com.cardgame.ui.UiTheme;
import com.cardgame.utils.Constants;

/**
 * Rest site screen with options:
 *   REST             – heal 30% max HP
 *   TRAIN ATTACK     – permanently increase base attack by +1
 *   FORTIFY DEFENSE  – permanently increase base defense by +1
 *   PROCEED          – leave without choosing an upgrade
 *
 * The player may choose only one option per rest site.
 */
public class RestScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private Texture bgTexture;
    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont subtitleFont;

    public RestScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        try {
            bgTexture = new Texture(Gdx.files.internal("IMAGES/Backgrounds/battle1.png"));
            bgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(new Color(0.12f, 0.07f, 0.05f, 1f));
            pm.fill();
            bgTexture = new Texture(pm);
            pm.dispose();
        }

        font = UiTheme.font(16f);
        subtitleFont = UiTheme.font(18f);

        titleFont = UiTheme.font(36f);
        titleFont.setColor(Color.ORANGE);

        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label("REST SITE", new Label.LabelStyle(titleFont, titleFont.getColor()));
        root.add(title).padBottom(12).colspan(3).row();

        RunManager rm = RunManager.getInstance();
        int curAtk = rm.getBaseAttackBonus();
        int curDef = rm.getBaseDefenseBonus();

        Label statsLabel = new Label("CURRENT BONUSES:   ATK +" + curAtk + "   |   DEF +" + curDef,
                new Label.LabelStyle(subtitleFont, new Color(0.9f, 0.88f, 0.82f, 1f)));
        root.add(statsLabel).padBottom(36).colspan(3).row();

        int healAmount = (int)(rm.getMaxHp() * 0.3f);

        // ── REST ─────────────────────────────────────────────
        TextButton healBtn = new TextButton("REST\n(Heal " + healAmount + " HP)",
                makeBtnStyle(new Color(0.45f, 0.9f, 0.55f, 1f), Color.WHITE));
        healBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rm.heal(healAmount);
                game.setScreen(new MapScreen(game));
            }
        });

        // ── TRAIN ATTACK (+1 ATK) ────────────────────────────
        TextButton atkBtn = new TextButton("TRAIN ATTACK\n(+1 Base Attack)",
                makeBtnStyle(new Color(1f, 0.5f, 0.38f, 1f), Color.WHITE));
        atkBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rm.addBaseAttackBonus(1);
                game.setScreen(new MapScreen(game));
            }
        });

        // ── FORTIFY DEFENSE (+1 DEF) ─────────────────────────
        TextButton defBtn = new TextButton("FORTIFY DEFENSE\n(+1 Base Defense)",
                makeBtnStyle(new Color(0.42f, 0.78f, 1f, 1f), Color.WHITE));
        defBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rm.addBaseDefenseBonus(1);
                game.setScreen(new MapScreen(game));
            }
        });

        // ── PROCEED ──────────────────────────────────────────
        TextButton leaveBtn = new TextButton("PROCEED\n(Leave Site)",
                makeBtnStyle(new Color(0.78f, 0.78f, 0.82f, 1f), Color.YELLOW));
        leaveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MapScreen(game));
            }
        });

        root.add(healBtn).size(230, 95).pad(14);
        root.add(atkBtn).size(230, 95).pad(14);
        root.add(defBtn).size(230, 95).pad(14).row();
        root.add(leaveBtn).size(230, 60).padTop(18).colspan(3).row();

        stage.addActor(root);
    }

    private TextButton.TextButtonStyle makeBtnStyle(Color color, Color hover) {
        TextButton.TextButtonStyle s = UiTheme.button(font);
        s.fontColor = color;
        s.overFontColor = hover;
        return s;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getViewport().apply();
        stage.getBatch().setProjectionMatrix(stage.getCamera().combined);

        if (bgTexture != null) {
            Batch batch = stage.getBatch();
            batch.begin();
            batch.setColor(0.35f, 0.32f, 0.32f, 1f);
            batch.draw(bgTexture, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
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

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (subtitleFont != null) subtitleFont.dispose();
    }
}
