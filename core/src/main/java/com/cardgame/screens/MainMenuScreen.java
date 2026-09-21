package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.data.CharacterData;
import com.cardgame.logic.RunManager;
import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.ui.UiTheme;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class MainMenuScreen implements Screen {
    private final CardBattlerGame game;
    private final List<Button> buttons = new ArrayList<>();
    private final List<Texture> buttonTextures = new ArrayList<>();
    private Stage stage;
    private Texture bgTexture;
    private Texture whiteTexture;
    private BitmapFont buttonFont;
    private BitmapFont messageFont;
    private Label message;
    private int focusedButton;
    private boolean transitioning;

    public MainMenuScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        transitioning = false;
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);
        Gdx.graphics.setVSync(Gdx.app.getPreferences("Locura").getBoolean("vsync", true));
        try {
            bgTexture = new Texture(Gdx.files.internal("IMAGES/MainMenuBackground.jpg"));
            bgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Image background = new Image(bgTexture);
            background.setScaling(Scaling.fit);
            background.setBounds(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
            background.setTouchable(Touchable.disabled);
            stage.addActor(background);
        } catch (Exception e) {
            Gdx.app.error("MainMenu", "Could not load menu background.", e);
        }
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        whiteTexture = new Texture(pixel);
        pixel.dispose();
        buttonFont = UiTheme.font(22f);
        messageFont = UiTheme.font(16f);
        buildUI();
    }

    private TextButton.TextButtonStyle buttonStyle() {
        TextureRegionDrawable pixel = new TextureRegionDrawable(whiteTexture);
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = buttonFont;
        style.fontColor = new Color(0.88f, 0.87f, 0.85f, 1f);
        style.overFontColor = Color.WHITE;
        style.downFontColor = Color.WHITE;
        style.up = pixel.tint(new Color(0, 0, 0, 0));
        style.over = pixel.tint(new Color(0.62f, 0.11f, 0.14f, 0.45f));
        style.down = pixel.tint(new Color(0.62f, 0.11f, 0.14f, 0.8f));
        style.focused = pixel.tint(new Color(0.62f, 0.11f, 0.14f, 0.3f));
        return style;
    }

    private void buildUI() {
        // Match the start hitbox to the words printed in the aspect-fitted artwork.
        float imageWidth = Constants.VIEWPORT_WIDTH;
        float imageHeight = Constants.VIEWPORT_HEIGHT;
        if (bgTexture != null) {
            float scale = Math.min(imageWidth / bgTexture.getWidth(), imageHeight / bgTexture.getHeight());
            imageWidth = bgTexture.getWidth() * scale;
            imageHeight = bgTexture.getHeight() * scale;
        }
        float imageX = (Constants.VIEWPORT_WIDTH - imageWidth) / 2f;
        float imageY = (Constants.VIEWPORT_HEIGHT - imageHeight) / 2f;
        Button start = addButton("PRESS START", "IMAGES/menu_buttons/press_start.png", this::startNewRun);
        start.setName("Start game");
        // Cover the static lettering in the background so only the animated label is visible.
        start.getStyle().up = new TextureRegionDrawable(whiteTexture).tint(new Color(0.04f, 0.04f, 0.05f, 1f));
        start.getStyle().over = new TextureRegionDrawable(whiteTexture).tint(new Color(0.28f, 0.06f, 0.08f, 1f));
        start.getStyle().focused = start.getStyle().over;
        start.getStyle().down = new TextureRegionDrawable(whiteTexture).tint(new Color(0.45f, 0.07f, 0.1f, 1f));
        start.setBounds(imageX + imageWidth * 0.37f, imageY + imageHeight * 0.043f,
                imageWidth * 0.26f, imageHeight * 0.065f);

        Button load = addButton("LOAD GAME", "IMAGES/menu_buttons/load_game.png", () -> message.setText(
                "No saved games available. Select PRESS START to begin a new run."));
        Button options = addButton("OPTIONS", "IMAGES/menu_buttons/options.png", () -> changeScreen(new SettingsScreen(game)));
        Button exit = addButton("EXIT GAME", "IMAGES/menu_buttons/exit_game.png", () -> {
            if (!transitioning) {
                transitioning = true;
                Gdx.app.postRunnable(() -> Gdx.app.exit());
            }
        });
        load.setBounds(360f, 4f, 220f, 40f);
        options.setBounds(610f, 4f, 220f, 40f);
        exit.setBounds(860f, 4f, 220f, 40f);

        message = new Label("",
                new Label.LabelStyle(messageFont, Color.LIGHT_GRAY));
        message.setAlignment(Align.center);
        message.setWrap(true);
        message.setBounds(180f, 120f, Constants.VIEWPORT_WIDTH - 360f, 54f);
        message.setTouchable(Touchable.disabled);
        stage.addActor(message);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (transitioning) return true;
                if (keycode == Input.Keys.TAB || keycode == Input.Keys.RIGHT || keycode == Input.Keys.DOWN) {
                    boolean backwards = keycode == Input.Keys.TAB
                            && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT));
                    focusButton(focusedButton + (backwards ? -1 : 1));
                    return true;
                }
                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.UP) {
                    focusButton(focusedButton - 1);
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    buttons.get(focusedButton).fire(new ChangeListener.ChangeEvent());
                    return true;
                }
                return false;
            }
        });
        focusButton(0);
    }

    private Button addButton(String text, String imagePath, Runnable action) {
        Button button;
        try {
            button = new AnimatedMenuButton(buttonStyle(), loadButtonFrames(imagePath));
        } catch (Exception e) {
            Gdx.app.error("MainMenu", "Could not load animated button: " + imagePath, e);
            button = new TextButton(text, buttonStyle());
        }
        button.setName(text);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!transitioning) action.run();
            }
        });
        button.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) focusButton(buttons.indexOf(event.getListenerActor()));
            }
        });
        buttons.add(button);
        stage.addActor(button);
        return button;
    }

    private TextureRegion[] loadButtonFrames(String imagePath) {
        // Each supplied PNG has two vertically stacked versions of the same label.
        Pixmap pixels = new Pixmap(Gdx.files.internal(imagePath));
        try {
            Texture texture = new Texture(pixels);
            buttonTextures.add(texture);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            TextureRegion[] frames = new TextureRegion[2];
            for (int row = 0; row < frames.length; row++) {
                int top = row * pixels.getHeight() / 2;
                int bottom = (row + 1) * pixels.getHeight() / 2;
                int minX = pixels.getWidth();
                int minY = bottom;
                int maxX = -1;
                int maxY = -1;
                // Ignore transparent margins; source PNGs remain untouched.
                for (int y = top; y < bottom; y++) {
                    for (int x = 0; x < pixels.getWidth(); x++) {
                        if ((pixels.getPixel(x, y) & 0xff) >= 32) {
                            minX = Math.min(minX, x);
                            minY = Math.min(minY, y);
                            maxX = Math.max(maxX, x);
                            maxY = Math.max(maxY, y);
                        }
                    }
                }
                if (maxX < minX || maxY < minY) {
                    throw new IllegalArgumentException("Menu button frame is empty: " + imagePath);
                }
                frames[row] = new TextureRegion(texture, minX, minY, maxX - minX + 1, maxY - minY + 1);
            }
            return frames;
        } finally {
            pixels.dispose();
        }
    }

    private static final class AnimatedMenuButton extends Button {
        private final Animation<TextureRegion> animation;
        private final TextureRegionDrawable drawable;
        private final Image image;
        private float stateTime;

        private AnimatedMenuButton(ButtonStyle style, TextureRegion[] frames) {
            super(style);
            animation = new Animation<>(0.35f, frames);
            animation.setPlayMode(Animation.PlayMode.LOOP);
            drawable = new TextureRegionDrawable(frames[0]);
            image = new Image(drawable, Scaling.fit);
            image.setTouchable(Touchable.disabled);
            add(image).grow().minSize(0f).pad(5f, 10f, 5f, 10f);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime = (stateTime + delta) % animation.getAnimationDuration();
            TextureRegion frame = animation.getKeyFrame(stateTime, true);
            if (drawable.getRegion() != frame) {
                drawable.setRegion(frame);
                image.invalidate();
            }
        }
    }

    private void focusButton(int index) {
        focusedButton = Math.floorMod(index, buttons.size());
        stage.setKeyboardFocus(buttons.get(focusedButton));
    }

    private void startNewRun() {
        if (game.getAllCharacters().isEmpty()) {
            message.setText("Unable to start: character data could not be loaded.");
            return;
        }
        CharacterData character = game.getAllCharacters().get(0);
        List<AbstractCard> starterDeck = new ArrayList<>();
        for (AbstractCard card : game.getAllCards()) {
            int copies = card.name().equalsIgnoreCase("Scalpel") ? 5
                    : card.name().equalsIgnoreCase("Cower") ? 4
                    : card.name().equalsIgnoreCase("Pipe") ? 1
                    : card.name().equalsIgnoreCase("Focus") ? 1 : 0;
            for (int i = 0; i < copies; i++) starterDeck.add(card.makeCopy());
        }
        if (starterDeck.isEmpty()) {
            message.setText("Unable to start: starter cards could not be loaded.");
            return;
        }
        RunManager.getInstance().startNewRun(character, starterDeck);
        changeScreen(new MapScreen(game));
    }

    private void changeScreen(Screen nextScreen) {
        if (transitioning) return;
        transitioning = true;
        // Finish input dispatch before disposing this stage.
        Gdx.app.postRunnable(() -> game.setScreen(nextScreen));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.03f, 0.03f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getViewport().apply();
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
        if (stage != null) {
            if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null);
            stage.dispose();
            stage = null;
        }
        if (bgTexture != null) { bgTexture.dispose(); bgTexture = null; }
        if (whiteTexture != null) { whiteTexture.dispose(); whiteTexture = null; }
        if (buttonFont != null) { buttonFont.dispose(); buttonFont = null; }
        if (messageFont != null) { messageFont.dispose(); messageFont = null; }
        for (Texture texture : buttonTextures) texture.dispose();
        buttonTextures.clear();
        buttons.clear();
    }
}
