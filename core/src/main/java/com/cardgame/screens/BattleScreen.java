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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.data.CardData;
import com.cardgame.logic.*;
import com.cardgame.logic.actions.AttackAction;
import com.cardgame.logic.actions.PlayCardAction;
import com.cardgame.logic.events.*;
import com.cardgame.ui.BoardActor;
import com.cardgame.ui.CardActor;
import com.cardgame.ui.HUD;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The main battle screen.
 *
 * <p>Layout (1280 × 720):
 * <pre>
 *  ┌──────────────────────────────────────────────────────────────┐ y=720
 *  │  Opponent HP bar (HUD)                                       │ y=676
 *  ├──────────────────────────────────────────────────────────────┤
 *  │  Opponent board zone              (BoardActor, player 1)     │ y=490–660
 *  ├──────────────────────────────────────────────────────────────┤
 *  │  Separator / turn label (HUD)                                │ y=360
 *  ├──────────────────────────────────────────────────────────────┤
 *  │  Player board zone                (BoardActor, player 0)     │ y=160–330
 *  ├──────────────────────────────────────────────────────────────┤
 *  │  Player hand zone                 (hand CardActors)          │ y=44–160
 *  ├──────────────────────────────────────────────────────────────┤
 *  │  Player HP / mana bar (HUD)                                  │ y=0–44
 *  └──────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><b>Event loop</b>: {@link #render} drains {@code gameState.eventQueue}
 * each frame and dispatches to {@link #handleEvent}.
 * <p>
 * <b>Hard rule</b>: game logic lives entirely in {@code logic/}. This class
 * wires UI events to {@link com.cardgame.logic.actions.GameAction} instances
 * and reacts to the resulting {@link GameEvent}s.
 */
public class BattleScreen implements Screen {

    // ── Injected ───────────────────────────────────────────────────────────────
    private final CardBattlerGame game;

    // ── Scene2D ───────────────────────────────────────────────────────────────
    private Stage      stage;
    private BoardActor playerBoard;
    private BoardActor opponentBoard;
    private HUD        hud;
    private Group      handGroup;

    // ── Game logic ────────────────────────────────────────────────────────────
    private GameState   gameState;
    private TurnManager turnManager;

    // ── Selection state (purely visual / input routing) ───────────────────────
    private CardActor selectedHandCard  = null;  // card in hand, ready to play
    private CardActor selectedBoardCard = null;  // friendly minion, ready to attack

    // ── Background ────────────────────────────────────────────────────────────
    private Texture bgTexture;
    private Texture zoneDivider;

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private BitmapFont font;

    // ── Hand actors ───────────────────────────────────────────────────────────
    private final List<CardActor> handActors = new ArrayList<>();

    public BattleScreen(CardBattlerGame game) {
        this.game = game;
    }

    // ── Screen lifecycle ───────────────────────────────────────────────────────

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        buildBackground();

        // ── Initialise game state ─────────────────────────────────────────────
        gameState   = new GameState();
        turnManager = new TurnManager();
        buildDecks(game.getCardTemplates());

        // ── Build scene ───────────────────────────────────────────────────────
        opponentBoard = new BoardActor(1, this::onOpponentBoardCardClicked);
        opponentBoard.setBounds(0, 490, Constants.VIEWPORT_WIDTH, 160f);

        playerBoard = new BoardActor(0, this::onPlayerBoardCardClicked);
        playerBoard.setBounds(0, 160, Constants.VIEWPORT_WIDTH, 160f);

        handGroup = new Group();
        handGroup.setBounds(0, 44, Constants.VIEWPORT_WIDTH, 116f);

        hud = new HUD(gameState, this::onEndTurn);
        hud.setBounds(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        stage.addActor(hud);
        stage.addActor(opponentBoard);
        stage.addActor(playerBoard);
        stage.addActor(handGroup);

        // ── Start game ────────────────────────────────────────────────────────
        List<GameEvent> startEvents = turnManager.startGame(gameState);
        gameState.pushEvents(startEvents);
    }

    // ── Deck construction ─────────────────────────────────────────────────────

    private void buildDecks(Map<String, CardData> templates) {
        List<CardData> allCards = new ArrayList<>(templates.values());
        if (allCards.isEmpty()) return;

        // Give each player a shuffled hand of all cards, repeated to fill deck
        for (int p = 0; p < 2; p++) {
            GameState.PlayerState ps = gameState.getPlayer(p);
            for (int i = 0; i < 20; i++) {
                ps.deck.add(allCards.get(i % allCards.size()));
            }
        }
    }

    // ── Input callbacks ────────────────────────────────────────────────────────

    private void onEndTurn() {
        clearAllSelections();
        List<GameEvent> events = turnManager.endTurn(gameState);
        gameState.pushEvents(events);
    }

    private void onHandCardClicked(CardActor actor) {
        if (gameState.getCurrentPlayer() != 0) return; // not player's turn

        if (selectedHandCard == actor) {
            // Deselect
            actor.setSelected(false);
            selectedHandCard = null;
        } else {
            clearAllSelections();
            selectedHandCard = actor;
            actor.setSelected(true);
        }
    }

    private void onPlayerBoardCardClicked(CardActor actor) {
        if (gameState.getCurrentPlayer() != 0) return;

        if (selectedHandCard != null) {
            // Tried to "target" friendly minion after selecting hand card — not applicable for non-targeted battlecries
            return;
        }

        // Select friendly minion for attacking
        if (selectedBoardCard == actor) {
            actor.setSelected(false);
            selectedBoardCard = null;
        } else {
            clearAllSelections();
            selectedBoardCard = actor;
            actor.setSelected(true);
        }
    }

    private void onOpponentBoardCardClicked(CardActor actor) {
        if (gameState.getCurrentPlayer() != 0) return;

        if (selectedHandCard != null) {
            // Play card from hand — position doesn't matter for non-targeted cards in this scaffold
            tryPlayCard(selectedHandCard.getCard());
            clearAllSelections();
            return;
        }

        if (selectedBoardCard != null) {
            // Attack opponent minion
            tryAttack(selectedBoardCard.getCard(), actor.getCard());
            clearAllSelections();
        }
    }

    // ── Action dispatch ────────────────────────────────────────────────────────

    private void tryPlayCard(CardInstance card) {
        try {
            List<GameEvent> events = new PlayCardAction(0, card, -1).execute(gameState);
            gameState.pushEvents(events);
        } catch (IllegalStateException e) {
            Gdx.app.log("BattleScreen", "PlayCard failed: " + e.getMessage());
        }
    }

    private void tryAttack(CardInstance attacker, CardInstance defender) {
        try {
            List<GameEvent> events = new AttackAction(attacker, defender).execute(gameState);
            gameState.pushEvents(events);
        } catch (IllegalStateException e) {
            Gdx.app.log("BattleScreen", "Attack failed: " + e.getMessage());
        }
    }

    private void clearAllSelections() {
        if (selectedHandCard  != null) { selectedHandCard.setSelected(false);  selectedHandCard  = null; }
        if (selectedBoardCard != null) { selectedBoardCard.setSelected(false); selectedBoardCard = null; }
        playerBoard.clearSelection();
        opponentBoard.clearSelection();
    }

    // ── Event handling ────────────────────────────────────────────────────────

    /**
     * Drain the event queue and update the UI accordingly.
     * Called once per {@link #render} frame.
     */
    private void processEventQueue() {
        GameEvent event;
        while ((event = gameState.pollEvent()) != null) {
            handleEvent(event);
        }
    }

    private void handleEvent(GameEvent event) {
        if (event instanceof CardPlayedEvent cpe) {
            Gdx.app.log("Event", "Card played: " + cpe.card().getTemplate().name());
        } else if (event instanceof CardDiedEvent cde) {
            Gdx.app.log("Event", "Card died: " + cde.card().getTemplate().name());
        } else if (event instanceof CardDrawnEvent cde) {
            Gdx.app.log("Event", "Card drawn: " + cde.card().getTemplate().name());
        } else if (event instanceof TurnChangedEvent tce) {
            Gdx.app.log("Event", "Turn → player " + tce.newCurrentPlayer());
        } else if (event instanceof ManaChangedEvent mce) {
            Gdx.app.log("Event", "Mana: " + mce.currentMana() + "/" + mce.maxMana());
        } else if (event instanceof GameOverEvent goe) {
            onGameOver(goe.winnerIndex());
        }
    }

    private void onGameOver(int winner) {
        Gdx.app.log("BattleScreen", "Game over — player " + winner + " wins!");
        // Future: transition to GameOverScreen
        game.setScreen(new MainMenuScreen(game));
    }

    // ── render ─────────────────────────────────────────────────────────────────

    @Override
    public void render(float delta) {
        // Clear
        Gdx.gl.glClearColor(0.08f, 0.09f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background zone divider lines via SpriteBatch before stage
        stage.getBatch().begin();
        drawZoneDividers();
        stage.getBatch().end();

        // Drain event queue
        processEventQueue();

        // Sync UI with game state
        opponentBoard.syncWithState(gameState);
        playerBoard.syncWithState(gameState);
        syncHandActors();
        hud.update(gameState);

        // Scene2D tick + draw
        stage.act(delta);
        stage.draw();
    }

    private void drawZoneDividers() {
        Batch b = stage.getBatch();
        b.setColor(new Color(0.3f, 0.3f, 0.5f, 0.5f));
        // Dividers between zones
        b.draw(zoneDivider, 0, 160, Constants.VIEWPORT_WIDTH, 2);
        b.draw(zoneDivider, 0, 330, Constants.VIEWPORT_WIDTH, 2);
        b.draw(zoneDivider, 0, 490, Constants.VIEWPORT_WIDTH, 2);
        b.draw(zoneDivider, 0, 650, Constants.VIEWPORT_WIDTH, 2);
        b.setColor(1, 1, 1, 1);
    }

    /** Rebuild the hand actor row for player 0. */
    private void syncHandActors() {
        List<CardInstance> hand = gameState.getHand(0);

        // Remove actors for cards no longer in hand
        List<CardActor> toRemove = new ArrayList<>();
        for (CardActor ca : handActors) {
            if (!hand.contains(ca.getCard())) toRemove.add(ca);
        }
        for (CardActor ca : toRemove) {
            handGroup.removeActor(ca);
            handActors.remove(ca);
            if (ca == selectedHandCard) selectedHandCard = null;
            ca.dispose();
        }

        // Add actors for newly-drawn cards
        for (CardInstance ci : hand) {
            boolean exists = handActors.stream().anyMatch(ca -> ca.getCard() == ci);
            if (!exists) {
                CardActor ca = new CardActor(ci, this::onHandCardClicked);
                ca.setSize(Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
                handGroup.addActor(ca);
                handActors.add(ca);
            }
        }

        // Layout
        float totalW = hand.size() * (Constants.CARD_WIDTH + Constants.CARD_GAP) - Constants.CARD_GAP;
        float startX = (Constants.VIEWPORT_WIDTH - totalW) / 2f;
        for (int i = 0; i < hand.size(); i++) {
            CardInstance ci    = hand.get(i);
            final int    index = i;
            handActors.stream()
                      .filter(ca -> ca.getCard() == ci)
                      .findFirst()
                      .ifPresent(ca -> {
                          float x = startX + index * (Constants.CARD_WIDTH + Constants.CARD_GAP);
                          ca.setPosition(x, 0);
                      });
        }
    }

    private void buildBackground() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        bgTexture   = new Texture(pm);
        zoneDivider = new Texture(pm);
        pm.dispose();
    }

    // ── Screen boilerplate ────────────────────────────────────────────────────

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        for (CardActor ca : handActors) ca.dispose();
        handActors.clear();
        if (hud       != null) { hud.disposeResources(); }
        if (stage     != null) { stage.dispose(); stage = null; }
        if (bgTexture != null) { bgTexture.dispose(); }
        if (zoneDivider != null) { zoneDivider.dispose(); }
        if (font      != null) { font.dispose(); }
    }
}
