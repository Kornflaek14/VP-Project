package com.cardgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.cardgame.utils.Constants;

/**
 * A semi-transparent dark overlay that simulates a pause/blur effect.
 * Provides three buttons: Resume, End Run, Exit Game.
 */
public class PauseOverlay extends Group {

    public interface PauseCallback {
        void onResume();
        void onEndRun();
        void onExitGame();
    }

    private final Texture overlayTex;
    private final BitmapFont titleFont;
    private final BitmapFont buttonFont;

    public PauseOverlay(final PauseCallback callback) {
        setSize(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        setVisible(false);
        setTouchable(Touchable.childrenOnly);

        // Dark semi-transparent overlay texture
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0f, 0f, 0f, 0.75f));
        pm.fill();
        overlayTex = new Texture(pm);
        pm.dispose();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);
        titleFont.setColor(new Color(0.96f, 0.84f, 0.38f, 1f));

        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(2.0f);

        // Build UI
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, titleFont.getColor());
        Label title = new Label("PAUSED", titleStyle);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = buttonFont;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = new Color(0.96f, 0.84f, 0.38f, 1f);

        TextButton resumeBtn = new TextButton("RESUME", btnStyle);
        resumeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                callback.onResume();
            }
        });

        TextButton endRunBtn = new TextButton("END RUN", btnStyle);
        endRunBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                callback.onEndRun();
            }
        });

        TextButton exitBtn = new TextButton("EXIT GAME", btnStyle);
        exitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                callback.onExitGame();
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.center();
        root.setSize(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        root.add(title).padBottom(60).row();
        root.add(resumeBtn).size(300, 70).padBottom(25).row();
        root.add(endRunBtn).size(300, 70).padBottom(25).row();
        root.add(exitBtn).size(300, 70).row();

        addActor(root);

        // Consume all touch events so they don't pass through
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true; // consume
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!isVisible()) return;
        // Draw dark overlay
        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(overlayTex, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        // Draw children (buttons)
        super.draw(batch, parentAlpha);
    }

    public void toggle() {
        setVisible(!isVisible());
        setTouchable(isVisible() ? Touchable.childrenOnly : Touchable.disabled);
    }

    public void show() {
        setVisible(true);
        setTouchable(Touchable.childrenOnly);
    }

    public void hide() {
        setVisible(false);
        setTouchable(Touchable.disabled);
    }

    public void disposeResources() {
        overlayTex.dispose();
        titleFont.dispose();
        buttonFont.dispose();
    }
}
