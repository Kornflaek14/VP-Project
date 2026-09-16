package com.cardgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.cardgame.CardBattlerGame;
import com.cardgame.logic.RunManager;
import com.cardgame.logic.monsters.CrawlingEye;
import com.cardgame.logic.monsters.FleshAmalgam;
import com.cardgame.logic.monsters.FrenziedPatient;
import com.cardgame.logic.monsters.MonsterGroup;
import com.cardgame.logic.potions.AdrenalineSyringe;
import com.cardgame.logic.potions.SteroidAmpoule;
import com.cardgame.logic.potions.VialOfAcid;
import com.cardgame.logic.rooms.BossRoom;
import com.cardgame.logic.rooms.EliteRoom;
import com.cardgame.logic.rooms.MonsterRoom;
import com.cardgame.logic.rooms.RestRoom;
import com.cardgame.logic.rooms.ShopRoom;
import com.cardgame.logic.rooms.TreasureRoom;
import com.cardgame.screens.BattleScreen;
import com.cardgame.screens.DeckViewerScreen;
import com.cardgame.screens.MainMenuScreen;
import com.cardgame.screens.MapScreen;
import com.cardgame.screens.RewardScreen;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Developer Mode Overlay.
 * Allows teleporting to any room in the game at any moment, testing monster/boss encounters,
 * inspecting screens, and applying testing cheats.
 * Can be toggled at any time with F1 or ~ (Grave).
 */
public class DevModeOverlay extends Group {

    public interface DevCloseCallback {
        void onClose();
    }

    private final CardBattlerGame game;
    private final DevCloseCallback closeCallback;

    private final Texture bgTexture;
    private final Texture btnNormalTex;
    private final Texture btnHoverTex;
    private final Texture btnActionTex;
    private final Texture btnCloseTex;

    private final BitmapFont titleFont;
    private final BitmapFont headerFont;
    private final BitmapFont bodyFont;

    private final Label statusLabel;
    private final List<Texture> ownedTextures = new ArrayList<>();

    public DevModeOverlay(final CardBattlerGame game, final DevCloseCallback closeCallback) {
        this.game = game;
        this.closeCallback = closeCallback;

        setSize(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        setVisible(false);
        setTouchable(Touchable.childrenOnly);

        // Dark modal background
        bgTexture = createSolidTexture(new Color(0.04f, 0.06f, 0.10f, 0.93f));
        btnNormalTex = createBorderedTexture(new Color(0.12f, 0.16f, 0.24f, 1f), new Color(0.24f, 0.35f, 0.52f, 1f));
        btnHoverTex = createBorderedTexture(new Color(0.18f, 0.25f, 0.38f, 1f), new Color(0.96f, 0.84f, 0.38f, 1f));
        btnActionTex = createBorderedTexture(new Color(0.12f, 0.28f, 0.20f, 1f), new Color(0.30f, 0.75f, 0.45f, 1f));
        btnCloseTex = createBorderedTexture(new Color(0.35f, 0.12f, 0.14f, 1f), new Color(0.85f, 0.30f, 0.35f, 1f));

        titleFont = new BitmapFont();
        titleFont.getData().setScale(1.8f);
        titleFont.setColor(new Color(0.96f, 0.84f, 0.38f, 1f));

        headerFont = new BitmapFont();
        headerFont.getData().setScale(1.3f);
        headerFont.setColor(new Color(0.40f, 0.80f, 1.0f, 1f));

        bodyFont = new BitmapFont();
        bodyFont.getData().setScale(1.05f);

        // TextButton styles
        TextButton.TextButtonStyle defaultBtnStyle = createButtonStyle(btnNormalTex, btnHoverTex, Color.WHITE, new Color(0.96f, 0.84f, 0.38f, 1f));
        TextButton.TextButtonStyle actionBtnStyle = createButtonStyle(btnActionTex, btnHoverTex, new Color(0.85f, 1f, 0.85f, 1f), Color.WHITE);
        TextButton.TextButtonStyle closeBtnStyle = createButtonStyle(btnCloseTex, btnHoverTex, Color.WHITE, new Color(1f, 0.8f, 0.8f, 1f));

        // Status notification label
        Label.LabelStyle statusStyle = new Label.LabelStyle(bodyFont, new Color(0.5f, 1.0f, 0.6f, 1f));
        statusLabel = new Label("Press F1 or ~ to toggle at any time", statusStyle);

        // Build layout
        Table root = new Table();
        root.setFillParent(true);
        root.center().top().pad(25f, 40f, 25f, 40f);

        // Title row
        Label title = new Label("[ DEVELOPER MODE - ROOM SELECTOR ]", new Label.LabelStyle(titleFont, titleFont.getColor()));
        Label subtitle = new Label("Jump into any room or battle encounter instantly to test its features", new Label.LabelStyle(bodyFont, Color.LIGHT_GRAY));
        root.add(title).padBottom(5f).row();
        root.add(subtitle).padBottom(20f).row();

        // 3-column table
        Table grid = new Table();
        grid.top();

        // Column 1: Combat Rooms & Encounters
        Table colCombat = new Table();
        colCombat.top();
        colCombat.add(new Label("COMBAT ENCOUNTERS", new Label.LabelStyle(headerFont, headerFont.getColor()))).padBottom(12f).row();
        
        addNavButton(colCombat, "FINAL BOSS ROOM", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            new BossRoom().onPlayerEntry(game);
            notifyStatus("Loaded Final Boss Room (with custom BG & Boss animations)!");
        });
        addNavButton(colCombat, "Frenzied Patient (Chained)", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new BattleScreen(game, new MonsterGroup(new FrenziedPatient(1000f, 250f))));
            notifyStatus("Loaded Battle: Frenzied Patient (Chained attack & idle)!");
        });
        addNavButton(colCombat, "Flesh Amalgam", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new BattleScreen(game, new MonsterGroup(new FleshAmalgam(1000f, 250f))));
            notifyStatus("Loaded Battle: Flesh Amalgam!");
        });
        addNavButton(colCombat, "Crawling Eye Duo", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new BattleScreen(game, new MonsterGroup(new CrawlingEye(850f, 250f), new CrawlingEye(1150f, 250f))));
            notifyStatus("Loaded Battle: Crawling Eye Duo!");
        });
        addNavButton(colCombat, "Elite Room", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            new EliteRoom().onPlayerEntry(game);
            notifyStatus("Loaded Elite Combat Room!");
        });
        addNavButton(colCombat, "Random Monster Room", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            new MonsterRoom().onPlayerEntry(game);
            notifyStatus("Loaded Random Monster Room!");
        });

        // Column 2: Non-Combat Rooms & Navigation
        Table colRooms = new Table();
        colRooms.top();
        colRooms.add(new Label("ROOMS & SCREENS", new Label.LabelStyle(headerFont, headerFont.getColor()))).padBottom(12f).row();

        addNavButton(colRooms, "Rest Site (Campfire)", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            new RestRoom().onPlayerEntry(game);
            notifyStatus("Loaded Rest Site (heal & upgrades)!");
        });
        addNavButton(colRooms, "Shop Room (Merchant)", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            new ShopRoom().onPlayerEntry(game);
            notifyStatus("Loaded Merchant Shop Room!");
        });
        addNavButton(colRooms, "Treasure Room (Relics)", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            new TreasureRoom().onPlayerEntry(game);
            notifyStatus("Loaded Treasure Room!");
        });
        addNavButton(colRooms, "World Map Screen", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new MapScreen(game));
            notifyStatus("Loaded World Map Screen!");
        });
        addNavButton(colRooms, "Reward Screen", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new RewardScreen(game));
            notifyStatus("Loaded Card & Gold Reward Screen!");
        });
        addNavButton(colRooms, "Deck Viewer Screen", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new DeckViewerScreen(game, game.getScreen()));
            notifyStatus("Loaded Deck Viewer Screen!");
        });
        addNavButton(colRooms, "Return to Main Menu", defaultBtnStyle, () -> {
            game.setScreen(new MainMenuScreen(game));
            notifyStatus("Returned to Main Menu!");
        });

        // Column 3: Cheats & Testing Tools
        Table colCheats = new Table();
        colCheats.top();
        colCheats.add(new Label("DEV TOOLBOX & CHEATS", new Label.LabelStyle(headerFont, headerFont.getColor()))).padBottom(12f).row();

        addToolButton(colCheats, "Heal HP to Full", actionBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            RunManager.getInstance().setCurrentHp(RunManager.getInstance().getMaxHp());
            notifyStatus("Player HP restored to " + RunManager.getInstance().getMaxHp() + "!");
        });
        addToolButton(colCheats, "+250 Gold", actionBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            RunManager.getInstance().addGold(250);
            notifyStatus("Added 250 Gold! Current: " + RunManager.getInstance().getGold());
        });
        addToolButton(colCheats, "Fill 3 Potions", actionBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            RunManager.getInstance().getPotions().clear();
            RunManager.getInstance().addPotion(new AdrenalineSyringe());
            RunManager.getInstance().addPotion(new VialOfAcid());
            RunManager.getInstance().addPotion(new SteroidAmpoule());
            notifyStatus("Added Adrenaline Syringe, Vial of Acid & Steroid Ampoule!");
        });
        addToolButton(colCheats, "+20 Max HP", actionBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            RunManager.getInstance().setMaxHp(RunManager.getInstance().getMaxHp() + 20);
            RunManager.getInstance().setCurrentHp(RunManager.getInstance().getCurrentHp() + 20);
            notifyStatus("Max HP increased to " + RunManager.getInstance().getMaxHp() + "!");
        });
        addToolButton(colCheats, "Add All Cards to Deck", actionBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            RunManager.getInstance().getDeck().addAll(game.getAllCards());
            notifyStatus("Added all " + game.getAllCards().size() + " game cards to your deck!");
        });

        grid.add(colCombat).width(360f).top().padRight(25f);
        grid.add(colRooms).width(360f).top().padRight(25f);
        grid.add(colCheats).width(340f).top();

        root.add(grid).padBottom(15f).row();
        root.add(statusLabel).padBottom(15f).row();

        // Close button
        TextButton closeBtn = new TextButton("RESUME GAME (ESC / F1)", closeBtnStyle);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                close();
            }
        });
        root.add(closeBtn).size(320f, 48f);

        addActor(root);

        // Consume touch events when visible and handle ESC key
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true; // Consume touch so background screen doesn't receive it
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.F1 || keycode == Input.Keys.GRAVE) {
                    close();
                    return true;
                }
                return false;
            }
        });
    }

    private void addNavButton(Table table, String text, TextButton.TextButtonStyle style, Runnable action) {
        TextButton btn = new TextButton(text, style);
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
                close();
            }
        });
        table.add(btn).size(340f, 42f).padBottom(8f).row();
    }

    private void addToolButton(Table table, String text, TextButton.TextButtonStyle style, Runnable action) {
        TextButton btn = new TextButton(text, style);
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        table.add(btn).size(320f, 42f).padBottom(8f).row();
    }

    private void notifyStatus(String msg) {
        statusLabel.setText(msg);
    }

    private TextButton.TextButtonStyle createButtonStyle(Texture normal, Texture hover, Color fontColor, Color hoverFontColor) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
        s.font = bodyFont;
        s.fontColor = fontColor;
        s.overFontColor = hoverFontColor;
        s.up = new TextureRegionDrawable(new TextureRegion(normal));
        s.over = new TextureRegionDrawable(new TextureRegion(hover));
        return s;
    }

    private Texture createSolidTexture(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        ownedTextures.add(tex);
        return tex;
    }

    private Texture createBorderedTexture(Color bg, Color border) {
        int w = 64, h = 32;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(bg);
        pm.fill();
        pm.setColor(border);
        pm.drawRectangle(0, 0, w, h);
        pm.drawRectangle(1, 1, w - 2, h - 2);
        Texture tex = new Texture(pm);
        pm.dispose();
        ownedTextures.add(tex);
        return tex;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!isVisible()) return;
        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(bgTexture, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        super.draw(batch, parentAlpha);
    }

    public boolean isOpen() {
        return isVisible();
    }

    public void show() {
        setVisible(true);
        setTouchable(Touchable.childrenOnly);
    }

    public void hide() {
        setVisible(false);
        setTouchable(Touchable.disabled);
    }

    public void toggle() {
        if (isOpen()) close();
        else show();
    }

    private void close() {
        hide();
        if (closeCallback != null) {
            closeCallback.onClose();
        }
    }

    public void disposeResources() {
        for (Texture t : ownedTextures) t.dispose();
        ownedTextures.clear();
        titleFont.dispose();
        headerFont.dispose();
        bodyFont.dispose();
    }
}
