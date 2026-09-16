package com.cardgame.ui;

import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.cardgame.CardBattlerGame;
import com.cardgame.logic.RunManager;
import com.cardgame.logic.monsters.ChainedPatient;
import com.cardgame.logic.monsters.CrawlingEye;
import com.cardgame.logic.monsters.FleshAmalgam;
import com.cardgame.logic.monsters.FrenziedPatient;
import com.cardgame.logic.monsters.HeadNurse;
import com.cardgame.logic.monsters.MaskedPatient;
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
    private static final Color IVORY = Color.valueOf("eee7da");
    private static final Color MUTED = Color.valueOf("aca99f");
    private static final Color COPPER = Color.valueOf("d6a67a");
    private static final Color SAGE = Color.valueOf("91b9aa");

    private final BitmapFont titleFont;
    private final BitmapFont headerFont;
    private final BitmapFont bodyFont;
    private final BitmapFont captionFont;

    private final Label statusLabel;
    private final List<Texture> ownedTextures = new ArrayList<>();

    public DevModeOverlay(final CardBattlerGame game, final DevCloseCallback closeCallback) {
        this.game = game;
        this.closeCallback = closeCallback;

        setSize(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        setVisible(false);
        setTouchable(Touchable.childrenOnly);

        // Dark modal background
        bgTexture = createSolidTexture(new Color(0.035f, 0.042f, 0.047f, 0.98f));
        titleFont = UiTheme.font(38f);
        headerFont = UiTheme.font(23f);
        bodyFont = UiTheme.font(17f);
        captionFont = UiTheme.font(12f);

        // TextButton styles
        TextButton.TextButtonStyle defaultBtnStyle = createButtonStyle(COPPER, false);
        TextButton.TextButtonStyle actionBtnStyle = createButtonStyle(SAGE, true);
        TextButton.TextButtonStyle closeBtnStyle = createButtonStyle(COPPER, false);

        // Status notification label
        Label.LabelStyle statusStyle = new Label.LabelStyle(captionFont, SAGE);
        statusLabel = new Label("Ready. Select a destination or apply a run adjustment.", statusStyle);
        statusLabel.setWrap(true);

        // Build layout
        Table root = new Table();
        root.setFillParent(true);
        root.center().pad(24f, 70f, 24f, 70f);

        // Title row
        root.add(new Label("LOCURA   /   DEVELOPMENT", new Label.LabelStyle(captionFont, COPPER)))
                .left().padBottom(10f).row();
        Label title = new Label("Developer tools", new Label.LabelStyle(titleFont, IVORY));
        Label subtitle = new Label("Stage an encounter. Explore the asylum. Shape your run.",
                new Label.LabelStyle(bodyFont, MUTED));
        root.add(title).left().padBottom(14f).row();
        root.add(subtitle).left().padBottom(22f).row();

        // 3-column table
        Table grid = new Table();
        grid.top();

        // Column 1: Combat Rooms & Encounters
        Table colCombat = new Table();
        styleColumn(colCombat);
        addSectionHeading(colCombat, "01  /  COMBAT", "Encounters", "Jump straight into a fight.", COPPER);
        
        addNavButton(colCombat, "The Surgeon (boss)", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            new BossRoom().onPlayerEntry(game);
            notifyStatus("Boss encounter loaded.");
        });
        addNavButton(colCombat, "Frenzied Patient", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new BattleScreen(game, new MonsterGroup(new FrenziedPatient(1000f, 250f))));
            notifyStatus("Frenzied Patient encounter loaded.");
        });
        addNavButton(colCombat, "Chained Patient", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new BattleScreen(game, new MonsterGroup(new ChainedPatient(1000f, 250f))));
            notifyStatus("Loaded Battle: Chained Patient!");
        });
        addNavButton(colCombat, "Patient Duo", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new BattleScreen(game, new MonsterGroup(new FrenziedPatient(850f, 250f), new ChainedPatient(1150f, 250f))));
            notifyStatus("Loaded Battle: Patient Duo (Frenzied & Chained)!");
        });
        addNavButton(colCombat, "Elite encounter", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            new EliteRoom().onPlayerEntry(game);
            notifyStatus("Loaded Elite Combat Room!");
        });
        addNavButton(colCombat, "Masked Patient (Elite)", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new BattleScreen(game, new MonsterGroup(new MaskedPatient(1000f, 250f))));
            notifyStatus("Loaded Battle: Masked Patient (Elite)!");
        });
        addNavButton(colCombat, "Head Nurse (Elite)", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new BattleScreen(game, new MonsterGroup(new HeadNurse(1000f, 250f))));
            notifyStatus("Loaded Battle: Head Nurse (Elite)!");
        });
        addNavButton(colCombat, "Random encounter", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            new MonsterRoom().onPlayerEntry(game);
            notifyStatus("Loaded Random Encounter!");
        });

        // Column 2: Non-Combat Rooms & Navigation
        Table colRooms = new Table();
        styleColumn(colRooms);
        addSectionHeading(colRooms, "02  /  EXPLORE", "Destinations", "Visit a room or inspect your deck.", COPPER);

        addNavButton(colRooms, "Rest site", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            new RestRoom().onPlayerEntry(game);
            notifyStatus("Loaded Rest Site (heal & upgrades)!");
        });
        addNavButton(colRooms, "Merchant", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            new ShopRoom().onPlayerEntry(game);
            notifyStatus("Loaded Merchant Shop Room!");
        });
        addNavButton(colRooms, "Treasure vault", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            new TreasureRoom().onPlayerEntry(game);
            notifyStatus("Loaded Treasure Room!");
        });
        addNavButton(colRooms, "Asylum map", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new MapScreen(game));
            notifyStatus("Loaded World Map!");
        });
        addNavButton(colRooms, "Battle rewards", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new RewardScreen(game));
            notifyStatus("Loaded Card & Gold Combat Rewards!");
        });
        addNavButton(colRooms, "Card collection", defaultBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            game.setScreen(new DeckViewerScreen(game, game.getScreen()));
            notifyStatus("Loaded Deck Viewer!");
        });
        addNavButton(colRooms, "Main menu", defaultBtnStyle, () -> {
            game.setScreen(new MainMenuScreen(game));
            notifyStatus("Returned to Main Menu!");
        });

        // Column 3: Cheats & Testing Tools
        Table colCheats = new Table();
        styleColumn(colCheats);
        addSectionHeading(colCheats, "03  /  ADJUST", "Run controls", "Apply changes to the saved run.", SAGE);

        addToolButton(colCheats, "Restore health", actionBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            RunManager.getInstance().setCurrentHp(RunManager.getInstance().getMaxHp());
            notifyStatus("Player HP restored to " + RunManager.getInstance().getMaxHp() + "!");
        });
        addToolButton(colCheats, "Grant 250 gold", actionBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            RunManager.getInstance().addGold(250);
            notifyStatus("Added 250 Gold! Current: " + RunManager.getInstance().getGold());
        });
        addToolButton(colCheats, "Refill potions", actionBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            RunManager.getInstance().getPotions().clear();
            RunManager.getInstance().addPotion(new AdrenalineSyringe());
            RunManager.getInstance().addPotion(new VialOfAcid());
            RunManager.getInstance().addPotion(new SteroidAmpoule());
            notifyStatus("Added Adrenaline Syringe, Vial of Acid & Steroid Ampoule!");
        });
        addToolButton(colCheats, "Raise max health by 20", actionBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            RunManager.getInstance().setMaxHp(RunManager.getInstance().getMaxHp() + 20);
            RunManager.getInstance().setCurrentHp(RunManager.getInstance().getCurrentHp() + 20);
            notifyStatus("Max HP increased to " + RunManager.getInstance().getMaxHp() + "!");
        });
        addToolButton(colCheats, "Add all cards", actionBtnStyle, () -> {
            RunManager.getInstance().ensureRunStarted(game);
            RunManager.getInstance().getDeck().addAll(game.getAllCards());
            notifyStatus("Added all " + game.getAllCards().size() + " game cards to your deck!");
        });

        grid.add(colCombat).width(380f).fillY().padRight(18f);
        grid.add(colRooms).width(380f).fillY().padRight(18f);
        grid.add(colCheats).width(380f).fillY();

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        root.add(scroll).width(1176f).height(534f).padBottom(18f).row();
        Table footer = new Table();
        footer.add(statusLabel).expandX().fillX().padRight(24f);
        footer.add(new Label("F1 / ~  Toggle     ESC  Close", new Label.LabelStyle(captionFont, MUTED))).right();
        root.add(footer).width(1176f).height(34f).padBottom(14f).row();

        // Close button
        TextButton closeBtn = new TextButton("Resume game", closeBtnStyle);
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
                if (keycode == Input.Keys.ESCAPE) {
                    close();
                    return true;
                }
                return true;
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
        btn.getLabel().setAlignment(Align.left);
        table.add(btn).size(340f, 46f).padBottom(8f).row();
    }

    private void addToolButton(Table table, String text, TextButton.TextButtonStyle style, Runnable action) {
        TextButton btn = new TextButton(text, style);
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        btn.getLabel().setAlignment(Align.left);
        table.add(btn).size(340f, 46f).padBottom(8f).row();
    }

    private void notifyStatus(String msg) {
        statusLabel.setText(msg);
    }

    private void styleColumn(Table table) {
        table.top().pad(20f);
        table.setBackground(UiTheme.panel(Color.valueOf("151c1e"), Color.valueOf("37403e")));
    }

    private void addSectionHeading(Table table, String eyebrow, String title, String description, Color accent) {
        table.add(new Label(eyebrow, new Label.LabelStyle(captionFont, accent))).left().padBottom(12f).row();
        table.add(new Label(title, new Label.LabelStyle(headerFont, IVORY))).left().padBottom(10f).row();
        table.add(new Label(description, new Label.LabelStyle(captionFont, MUTED))).left().padBottom(24f).row();
    }

    private TextButton.TextButtonStyle createButtonStyle(Color accent, boolean action) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
        s.font = bodyFont;
        s.fontColor = IVORY;
        s.overFontColor = accent;
        s.downFontColor = Color.WHITE;
        s.up = UiTheme.panel(Color.valueOf(action ? "20312d" : "22292a"), Color.valueOf(action ? "3d574e" : "424846"));
        s.over = UiTheme.panel(Color.valueOf(action ? "2b443b" : "38332d"), accent);
        s.down = UiTheme.panel(Color.valueOf("101817"), accent);
        s.focused = s.over;
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
        setTouchable(Touchable.enabled);
        toFront();
        if (getStage() != null) getStage().setKeyboardFocus(this);
    }

    public void hide() {
        if (getStage() != null && getStage().getKeyboardFocus() == this) getStage().setKeyboardFocus(null);
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
        captionFont.dispose();
    }
}
