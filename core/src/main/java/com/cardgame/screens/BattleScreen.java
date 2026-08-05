package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.data.CardData;
import com.cardgame.data.MonsterData;
import com.cardgame.logic.CombatResolver;
import com.cardgame.logic.GameState;
import com.cardgame.logic.RunManager;
import com.cardgame.logic.TurnManager;
import com.cardgame.logic.events.GameEvent;
import com.cardgame.ui.CardActor;
import com.cardgame.ui.HUD;
import com.cardgame.ui.HandArea;
import com.cardgame.ui.PauseOverlay;
import com.cardgame.utils.Constants;

import java.util.List;
import java.util.Random;

public class BattleScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private Texture bgTexture;
    private Texture monsterTexture;
    private Texture playerTexture;

    private GameState gameState;
    private TurnManager turnManager;
    private CombatResolver combatResolver;

    private HandArea handArea;
    private HUD hud;

    private PauseOverlay pauseOverlay;
    private boolean paused = false;

    public BattleScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));

        try {
            bgTexture = new Texture(Gdx.files.internal("IMAGES/play/playBackground.jpg"));
        } catch (Exception e) {}

        gameState = new GameState();
        turnManager = new TurnManager();
        combatResolver = new CombatResolver();

        // Init player from RunManager
        RunManager rm = RunManager.getInstance();
        gameState.initPlayer(rm.getCurrentHp(), rm.getMaxHp(), rm.getSelectedCharacter().energy(), rm.getDeck());

        // Load player character texture
        try {
            String charImage = rm.getSelectedCharacter().image();
            if (charImage != null && !charImage.isEmpty()) {
                playerTexture = new Texture(Gdx.files.internal(charImage));
            }
        } catch (Exception e) {
            // Try a fallback
            try {
                playerTexture = new Texture(Gdx.files.internal("IMAGES/play/character.png"));
            } catch (Exception e2) {}
        }

        // Init random monster for current floor level
        int level = rm.getCurrentNodeIndex() / 4 + 1; // 1-4 scale approx
        List<MonsterData> monsters = game.getMonstersForLevel(level);
        if (monsters.isEmpty()) monsters = game.getAllMonsters();

        MonsterData selectedMonster = monsters.get(new Random().nextInt(monsters.size()));
        gameState.initMonster(selectedMonster);

        try {
            if (selectedMonster.image() != null && !selectedMonster.image().isEmpty()) {
                monsterTexture = new Texture(Gdx.files.internal(selectedMonster.image()));
            }
        } catch (Exception e) {}

        buildUI();
        buildPauseOverlay();

        // Set up input: ESC key + stage
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    togglePause();
                    return true;
                }
                return false;
            }
        });
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        // Start combat
        turnManager.startCombat(gameState);
        updateUI();
    }

    private void togglePause() {
        paused = !paused;
        pauseOverlay.toggle();
    }

    private void buildPauseOverlay() {
        pauseOverlay = new PauseOverlay(new PauseOverlay.PauseCallback() {
            @Override
            public void onResume() {
                paused = false;
                pauseOverlay.hide();
            }
            @Override
            public void onEndRun() {
                game.setScreen(new MainMenuScreen(game));
            }
            @Override
            public void onExitGame() {
                Gdx.app.exit();
            }
        });
        stage.addActor(pauseOverlay);
    }

    private void buildUI() {
        handArea = new HandArea(new CardActor.OnClickCallback() {
            @Override
            public void onClick(CardActor actor) {
                if (paused) return;
                if (gameState.isPlayerTurn()) {
                    CardData card = actor.getCard();
                    List<GameEvent> events = combatResolver.playCard(gameState, card);
                    processEvents(events);
                    updateUI();
                }
            }
        });
        stage.addActor(handArea);

        hud = new HUD(gameState, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (paused) return;
                if (gameState.isPlayerTurn()) {
                    List<GameEvent> events = turnManager.endPlayerTurn(gameState);
                    processEvents(events);
                    updateUI();
                }
            }
        });
        stage.addActor(hud);
    }

    private void processEvents(List<GameEvent> events) {
        for (GameEvent e : events) {
            if (e instanceof com.cardgame.logic.events.GameOverEvent) {
                com.cardgame.logic.events.GameOverEvent goe = (com.cardgame.logic.events.GameOverEvent) e;
                RunManager.getInstance().setCurrentHp(gameState.playerHp);
                game.setScreen(new GameOverScreen(game, goe.winnerIndex()));
                return;
            }
        }
    }

    private void updateUI() {
        handArea.syncWithState(gameState);
        hud.update(gameState);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Batch batch = stage.getBatch();
        batch.begin();

        // ── Background ────────────────────────────────────────
        if (bgTexture != null) {
            batch.setColor(1, 1, 1, 1);
            batch.draw(bgTexture, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        }

        // ── Player character (left side) ──────────────────────
        if (playerTexture != null) {
            float pw = 250f;
            float ph = 300f;
            float px = hud.getPlayerX() - pw / 2f;
            float py = hud.getCharY();
            batch.setColor(1, 1, 1, 1);
            batch.draw(playerTexture, px, py, pw, ph);
        }

        // ── Monster (right side) ─────────────────────────────
        if (monsterTexture != null) {
            float mw = 280f;
            float mh = 320f;
            float mx = hud.getMonsterX() - mw / 2f;
            float my = hud.getCharY() - 20f;
            batch.setColor(1, 1, 1, 1);
            batch.draw(monsterTexture, mx, my, mw, mh);
        }

        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (monsterTexture != null) monsterTexture.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (hud != null) hud.disposeResources();
        if (handArea != null) handArea.disposeAll();
        if (pauseOverlay != null) pauseOverlay.disposeResources();
    }
}
