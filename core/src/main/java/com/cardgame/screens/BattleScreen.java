package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
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
import com.cardgame.utils.Constants;

import java.util.List;
import java.util.Random;

public class BattleScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private Texture bgTexture;
    private Texture monsterTexture;

    private GameState gameState;
    private TurnManager turnManager;
    private CombatResolver combatResolver;

    private HandArea handArea;
    private HUD hud;

    public BattleScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        try {
            bgTexture = new Texture(Gdx.files.internal("IMAGES/play/playBackground.jpg"));
        } catch (Exception e) {}

        gameState = new GameState();
        turnManager = new TurnManager();
        combatResolver = new CombatResolver();

        // Init player from RunManager
        RunManager rm = RunManager.getInstance();
        gameState.initPlayer(rm.getCurrentHp(), rm.getMaxHp(), rm.getSelectedCharacter().energy(), rm.getDeck());

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

        // Start combat
        turnManager.startCombat(gameState);
        updateUI();
    }

    private void buildUI() {
        handArea = new HandArea(new CardActor.OnClickCallback() {
            @Override
            public void onClick(CardActor actor) {
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
        if (bgTexture != null) {
            batch.setColor(1, 1, 1, 1);
            batch.draw(bgTexture, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        }
        if (monsterTexture != null) {
            // Draw monster roughly in top center
            float w = 300f;
            float h = 300f;
            batch.draw(monsterTexture, Constants.VIEWPORT_WIDTH / 2f - w/2f, Constants.VIEWPORT_HEIGHT / 2f - 50f, w, h);
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
        if (hud != null) hud.disposeResources();
        if (handArea != null) handArea.disposeAll();
    }
}
