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
import com.cardgame.logic.actions.PlayCardAction;
import com.cardgame.logic.events.*;
import com.cardgame.logic.ai.SimpleAI;
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
    private com.cardgame.ui.CardDetailOverlay cardDetailOverlay;
    
    private com.cardgame.ui.PileActor playerDrawPile;
    private com.cardgame.ui.PileActor playerDiscardPile;

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
        opponentBoard = new BoardActor(1, this::onOpponentBoardCardClicked, null);
        opponentBoard.setBounds(0, 490, Constants.VIEWPORT_WIDTH, 170);

        playerBoard = new BoardActor(0, this::onPlayerBoardCardClicked, this::onPlayerSlotClicked);
        playerBoard.setBounds(0, 160, Constants.VIEWPORT_WIDTH, 170);
        // ── Input mode reset on empty clicks ──────────────────────────────────
        handGroup = new Group();
        handGroup.setBounds(0, 44, Constants.VIEWPORT_WIDTH, 116f);

        hud = new HUD(gameState, this::onEndTurn);
        hud.setBounds(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);

        cardDetailOverlay = new com.cardgame.ui.CardDetailOverlay();
        cardDetailOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        cardDetailOverlay.setVisible(false);
        
        playerDrawPile = new com.cardgame.ui.PileActor("DECK");
        playerDrawPile.setPosition(20f, 20f);
        
        playerDiscardPile = new com.cardgame.ui.PileActor("GRAVE");
        playerDiscardPile.setPosition(Constants.VIEWPORT_WIDTH - Constants.CARD_WIDTH - 20f, 20f);

        stage.addActor(hud);
        stage.addActor(playerDrawPile);
        stage.addActor(playerDiscardPile);
        stage.addActor(opponentBoard);
        stage.addActor(playerBoard);
        stage.addActor(handGroup);
        stage.addActor(cardDetailOverlay);

        // ── Start game ────────────────────────────────────────────────────────
        List<GameEvent> startEvents = turnManager.startGame(gameState);
        gameState.pushEvents(startEvents);
    }

    // ── Deck construction ─────────────────────────────────────────────────────

    private void buildDecks(Map<String, CardData> templates) {
        List<CardData> allCards = new ArrayList<>(templates.values());
        if (allCards.isEmpty()) return;

        CardData testTube = templates.get("vial");

        for (int p = 0; p < 2; p++) {
            GameState.PlayerState ps = gameState.getPlayer(p);
            // Give 10 test tubes
            for (int i = 0; i < 10; i++) {
                if (testTube != null) ps.deck.add(testTube);
            }
            // Give 10 other cards
            for (int i = 0; i < 10; i++) {
                ps.deck.add(allCards.get(i % allCards.size()));
            }
            java.util.Collections.shuffle(ps.deck);
            
            // Ensure at least 2 test tubes at the top of the deck to avoid dead starts
            if (testTube != null) {
                ps.deck.add(0, testTube);
                ps.deck.add(0, testTube);
            }
        }
    }

    // ── Input State Machine ───────────────────────────────────────────────────
    private enum InputMode { SELECT, PLAY, SACRIFICE }
    private InputMode inputMode = InputMode.SELECT;
    private final List<CardInstance> sacrificeTargets = new ArrayList<>();
    
    // ── AI Timer ──────────────────────────────────────────────────────────────
    private float   aiTimer  = 1.0f;
    private boolean gameOver = false;
    private final SimpleAI ai = new SimpleAI();

    // ── Input callbacks ────────────────────────────────────────────────────────

    private void onEndTurn() {
        clearAllSelections();
        List<GameEvent> events = turnManager.endTurn(gameState);
        gameState.pushEvents(events);
    }

    private void onHandCardClicked(CardActor actor) {
        if (gameState.getCurrentPlayer() != 0) return;

        if (inputMode == InputMode.SELECT || inputMode == InputMode.PLAY || inputMode == InputMode.SACRIFICE) {
            if (selectedHandCard == actor) {
                clearAllSelections();
                return;
            }
            
            clearAllSelections(); // Resets mode to SELECT
            
            int bloodCost = actor.getCard().getTemplate().bloodCost();
            if (bloodCost > 0 && gameState.getPlayer(0).sacrificeCredit < bloodCost) {
                inputMode = InputMode.SACRIFICE;
                selectedHandCard = actor;
                sacrificeTargets.clear();
                actor.setSelected(true);
                int needed = bloodCost - gameState.getPlayer(0).sacrificeCredit;
                if (hud != null) hud.setInputModeMessage("SELECT " + needed + " MINIONS TO SACRIFICE");
            } else {
                if (selectedHandCard != null) {
                    selectedHandCard.setSelected(false);
                }
                selectedHandCard = actor;
                selectedHandCard.setSelected(true);
                
                cardDetailOverlay.setCard(actor.getCard());
                cardDetailOverlay.setVisible(true);

                if (hud != null) {
                    hud.setInputModeMessage("CLICK BOARD TO PLAY");
                }
            }
        } else {
            clearAllSelections();
        }
    }

    private void onPlayerBoardCardClicked(CardActor actor) {
        if (gameState.getCurrentPlayer() != 0) return;

        if (inputMode == InputMode.SACRIFICE) {
            if (!sacrificeTargets.contains(actor.getCard())) {
                sacrificeTargets.add(actor.getCard());
                actor.setSelected(true); // visually highlight the target
                int needed = selectedHandCard.getCard().getTemplate().bloodCost() - gameState.getPlayer(0).sacrificeCredit;
                if (sacrificeTargets.size() >= needed) {
                    try {
                        List<GameEvent> evts = new com.cardgame.logic.actions.SacrificeAction(0, sacrificeTargets).execute(gameState);
                        gameState.pushEvents(evts);
                        inputMode = InputMode.PLAY;
                        sacrificeTargets.clear();
                        playerBoard.clearSelection(); // clear highlights
                        if (hud != null) hud.setInputModeMessage("CLICK TO PLAY");
                    } catch (IllegalStateException e) {
                        clearAllSelections();
                        if (hud != null) hud.setInputModeMessage(e.getMessage().toUpperCase());
                    }
                } else {
                    if (hud != null) hud.setInputModeMessage("SELECT " + (needed - sacrificeTargets.size()) + " MORE");
                }
            }
        } else if (inputMode == InputMode.PLAY) {
            tryPlayCard(selectedHandCard.getCard(), -1, null);
            clearAllSelections();
        } else {
            clearAllSelections();
        }
    }

    private void onOpponentBoardCardClicked(CardActor actor) {
        if (gameState.getCurrentPlayer() != 0) return;

        if (inputMode == InputMode.PLAY) {
            tryPlayCard(selectedHandCard.getCard(), -1, actor.getCard());
            clearAllSelections();
        }
    }
    private void onPlayerSlotClicked(int index) {
        if (gameState.getCurrentPlayer() != 0) return;
        
        if (inputMode == InputMode.PLAY && selectedHandCard != null) {
            tryPlayCard(selectedHandCard.getCard(), index, null);
            clearAllSelections();
        }
    }

    // ── Action dispatch ────────────────────────────────────────────────────────

    private void tryPlayCard(CardInstance card, int position, CardInstance target) {
        try {
            List<GameEvent> events = new PlayCardAction(0, card, position, target).execute(gameState);
            gameState.pushEvents(events);
        } catch (IllegalStateException e) {
            Gdx.app.log("BattleScreen", "PlayCard failed: " + e.getMessage());
            if (hud != null) hud.setInputModeMessage(e.getMessage().toUpperCase());
        }
    }




    private void clearAllSelections() {
        inputMode = InputMode.SELECT;
        sacrificeTargets.clear();
        if (selectedHandCard  != null) { selectedHandCard.setSelected(false);  selectedHandCard  = null; }
        if (selectedBoardCard != null) { selectedBoardCard.setSelected(false); selectedBoardCard = null; }
        playerBoard.clearSelection();
        opponentBoard.clearSelection();
        
        cardDetailOverlay.setCard(null);
        cardDetailOverlay.setVisible(false);
        
        if (hud != null) {
            hud.setInputModeMessage("SELECT A CARD");
        }
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
        } else if (event instanceof GameOverEvent goe) {
            gameOver = true;
            String winner = goe.winnerIndex() == 0 ? "YOU WIN" : "OPPONENT WINS";
            Gdx.app.log("BattleScreen", "Game over — " + winner
                    + " (scale = " + gameState.getScaleBalance() + ")");
            if (hud != null) hud.setInputModeMessage(winner + "!");
            onGameOver(goe.winnerIndex());
        } else if (event instanceof com.cardgame.logic.events.DamageDealtEvent dde) {
            spawnFloatingText(dde.target(), "-" + dde.amount(), com.badlogic.gdx.graphics.Color.RED);
        } else if (event instanceof com.cardgame.logic.events.ScaleChangedEvent sce) {
            float yPos = (sce.dealingPlayer() == 1) ? 60f : Constants.VIEWPORT_HEIGHT - 60f;
            spawnFloatingText(Constants.VIEWPORT_WIDTH / 2f, yPos, "-" + sce.damageAmount(), com.badlogic.gdx.graphics.Color.RED);
        }
    }

    private void spawnFloatingText(com.cardgame.logic.CardInstance target, String text, com.badlogic.gdx.graphics.Color color) {
        CardActor actor = findCardActor(target);
        if (actor != null) {
            com.badlogic.gdx.math.Vector2 pos = actor.localToStageCoordinates(new com.badlogic.gdx.math.Vector2(actor.getWidth() / 2f, actor.getHeight() / 2f));
            spawnFloatingText(pos.x, pos.y, text, color);
        }
    }

    private void spawnFloatingText(float x, float y, String text, com.badlogic.gdx.graphics.Color color) {
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle style = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        style.font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        style.font.getData().setScale(1.5f);
        style.fontColor = color;
        com.badlogic.gdx.scenes.scene2d.ui.Label label = new com.badlogic.gdx.scenes.scene2d.ui.Label(text, style);
        label.setPosition(x - label.getPrefWidth() / 2f, y);
        label.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(0, 50, 1.0f, com.badlogic.gdx.math.Interpolation.fade),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(0.2f),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor()
        ));
        stage.addActor(label);
    }

    private CardActor findCardActor(com.cardgame.logic.CardInstance card) {
        for (com.badlogic.gdx.scenes.scene2d.Actor a : playerBoard.getChildren()) {
            if (a instanceof CardActor && ((CardActor) a).getCard() == card) return (CardActor) a;
        }
        for (com.badlogic.gdx.scenes.scene2d.Actor a : opponentBoard.getChildren()) {
            if (a instanceof CardActor && ((CardActor) a).getCard() == card) return (CardActor) a;
        }
        return null;
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

        // AI Tick (skip if game is over)
        if (!gameOver && gameState.getCurrentPlayer() == 1
                && turnManager.getCurrentPhase() == TurnManager.Phase.MAIN) {
            aiTimer -= delta;
            if (aiTimer <= 0) {
                com.cardgame.logic.actions.GameAction action = ai.getNextAction(gameState);
                if (action != null) {
                    try {
                        gameState.pushEvents(action.execute(gameState));
                    } catch (IllegalStateException e) {
                        Gdx.app.log("SimpleAI", "AI Action failed: " + e.getMessage());
                    }
                    aiTimer = 1.0f;
                } else {
                    onEndTurn(); // Force end turn
                    aiTimer = 1.0f;
                }
            }
        }

        // Sync UI with game state
        opponentBoard.syncWithState(gameState);
        playerBoard.syncWithState(gameState);
        syncHandActors();
        hud.setTurnNumber(turnManager.getTurnNumber());
        hud.update(gameState);
        
        playerDrawPile.setCount(gameState.getPlayer(0).deck.size());
        playerDrawPile.setVisible(true);
        playerDiscardPile.setCount(gameState.getPlayer(0).discardPile.size());
        playerDiscardPile.setVisible(true);

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
        if (cardDetailOverlay != null) { cardDetailOverlay.dispose(); }
        if (stage     != null) { stage.dispose(); stage = null; }
        if (bgTexture != null) { bgTexture.dispose(); }
        if (zoneDivider != null) { zoneDivider.dispose(); }
        if (playerDrawPile != null) { playerDrawPile.dispose(); }
        if (playerDiscardPile != null) { playerDiscardPile.dispose(); }
        if (font      != null) { font.dispose(); }
    }
}
