package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.data.CardData;
import com.cardgame.data.CardType;
import com.cardgame.logic.*;
import com.cardgame.logic.actions.PlayCardAction;
import com.cardgame.logic.actions.SacrificeAction;
import com.cardgame.logic.events.*;
import com.cardgame.ui.BoardActor;
import com.cardgame.ui.CardActor;
import com.cardgame.ui.HandArea;
import com.cardgame.ui.HUD;
import com.cardgame.ui.PileActor;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
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
 *  │  HP display / turn label (HUD)                               │ y=360
 *  ├──────────────────────────────────────────────────────────────┤
 *  │  Player board zone                (BoardActor, player 0)     │ y=160–330
 *  ├──────────────────────────────────────────────────────────────┤
 *  │  Player hand zone                 (HandArea, 6 slots)        │ y=44–160
 *  ├──────────────────────────────────────────────────────────────┤
 *  │  Player HP / resource bar (HUD)                              │ y=0–44
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
    private HandArea   handArea;

    // ── Game logic ────────────────────────────────────────────────────────────
    private GameState   gameState;
    private TurnManager turnManager;

    // ── UI helpers ────────────────────────────────────────────────────────────
    private PileActor playerDrawPile;
    private PileActor playerDiscardPile;

    // ── Background ────────────────────────────────────────────────────────────
    private Texture bgTexture;
    private Texture zoneDivider;

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private BitmapFont font;

    // ── Input state machine ───────────────────────────────────────────────────
    private enum InputMode { IDLE, DRAGGING, SACRIFICE }
    private InputMode inputMode = InputMode.IDLE;
    private CardActor draggingCard = null;
    private final List<CardInstance> sacrificeTargets = new ArrayList<>();
    private CardActor pendingSacrificeCard = null; // hand card waiting for sacrifices

    // ── AI Timer ──────────────────────────────────────────────────────────────
    private float   aiTimer  = 1.0f;
    private boolean gameOver = false;

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
        opponentBoard.setBounds(0, 490, Constants.VIEWPORT_WIDTH, 170);

        playerBoard = new BoardActor(0, this::onPlayerBoardCardClicked);
        playerBoard.setBounds(0, 160, Constants.VIEWPORT_WIDTH, 170);

        // ── Hand area (drag-based interaction) ────────────────────────────────
        handArea = new HandArea(0, new CardActor.CardInteractionCallback() {
            @Override public void onClick(CardActor actor)  { onHandCardClicked(actor); }
            @Override public void onDragStart(CardActor actor) { onCardDragStart(actor); }
            @Override public void onDrag(CardActor actor, float stageX, float stageY) { onCardDragging(actor, stageX, stageY); }
            @Override public void onDragEnd(CardActor actor, float stageX, float stageY) { onCardDrop(actor, stageX, stageY); }
        });
        handArea.setBounds(0, 44, Constants.VIEWPORT_WIDTH, 116f);

        hud = new HUD(gameState);
        hud.setBounds(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        
        playerDrawPile = new PileActor("DECK");
        playerDrawPile.setPosition(20f, 20f);
        
        playerDiscardPile = new PileActor("GRAVE");
        playerDiscardPile.setPosition(Constants.VIEWPORT_WIDTH - Constants.CARD_WIDTH - 20f, 20f);

        stage.addActor(hud);
        stage.addActor(playerDrawPile);
        stage.addActor(playerDiscardPile);
        stage.addActor(opponentBoard);
        stage.addActor(playerBoard);
        stage.addActor(handArea);

        // ── End Turn Button ───────────────────────────────────────────────────
        Label.LabelStyle btnStyle = new Label.LabelStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.GOLD;
        Label endTurnBtn = new Label("[ END TURN ]", btnStyle);
        endTurnBtn.setPosition(Constants.VIEWPORT_WIDTH - 180f, 60f);
        endTurnBtn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        endTurnBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (!gameOver && gameState.getCurrentPlayer() == 0 && inputMode == InputMode.IDLE) {
                    List<GameEvent> evts = turnManager.endTurn(gameState);
                    gameState.pushEvents(evts);
                }
            }
        });
        stage.addActor(endTurnBtn);

        // ── Start game ────────────────────────────────────────────────────────
        List<GameEvent> startEvents = turnManager.startGame(gameState);
        gameState.pushEvents(startEvents);
    }

    // ── Deck construction ─────────────────────────────────────────────────────

    private void buildDecks(Map<String, CardData> templates) {
        if (templates.isEmpty()) return;

        for (int p = 0; p < 2; p++) {
            GameState.PlayerState ps = gameState.getPlayer(p);
            
            int totalBloodCost = 0;
            CardData squirrelTemplate = templates.get("squirrel");

            for (CardData template : templates.values()) {
                // Ignore hardcoded deckCount for squirrels so we can calculate it dynamically
                if (template.id().equals("squirrel")) continue;
                
                int count = template.deckCount();
                for (int i = 0; i < count; i++) {
                    ps.deck.add(template);
                    totalBloodCost += template.bloodCost();
                }
            }
            
            Collections.shuffle(ps.deck);
            
            // Calculate dynamic squirrels: at least 6, 1 for every 3 blood cost
            int dynamicSquirrels = Math.max(6, Math.min(10, totalBloodCost / 3));
            
            // Evenly distribute the squirrels throughout the deck
            if (squirrelTemplate != null) {
                // We want to guarantee exactly 2 at the very start, so we subtract 2 from the pool
                int deckSize = ps.deck.size();
                int squirrelsToDistribute = dynamicSquirrels - 2;
                if (squirrelsToDistribute > 0 && deckSize > 0) {
                    // Calculate insertion interval to space them evenly
                    float interval = (float) deckSize / squirrelsToDistribute;
                    // Insert from back to front so indices don't shift
                    for (int i = squirrelsToDistribute - 1; i >= 0; i--) {
                        int index = Math.round(i * interval);
                        ps.deck.add(index, squirrelTemplate);
                    }
                }
                
                // Finally, add the 2 guaranteed squirrels to the start (index 0)
                ps.deck.add(0, squirrelTemplate);
                ps.deck.add(0, squirrelTemplate);
            }
        }
    }

    // ── Drag & Drop handlers ──────────────────────────────────────────────────

    private void onCardDragStart(CardActor actor) {
        if (gameOver || gameState.getCurrentPlayer() != 0) return;
        
        // Check if card needs blood cost and we don't have enough
        int bloodCost = actor.getCard().getTemplate().bloodCost();
        if (bloodCost > 0 && gameState.getPlayer(0).sacrificeCredit < bloodCost) {
            // Need to sacrifice first — enter sacrifice mode
            enterSacrificeMode(actor);
            return;
        }
        
        inputMode = InputMode.DRAGGING;
        draggingCard = actor;
        actor.setLifted(true);
        actor.toFront();
        
        // Highlight valid board slots
        if (actor.getCard().getTemplate().cardType() == CardType.UNIT) {
            playerBoard.highlightEmptySlots(gameState);
        }
        
        if (hud != null) hud.setInputModeMessage("DROP ON A SLOT TO PLAY");
    }

    private void onCardDragging(CardActor actor, float stageX, float stageY) {
        if (inputMode != InputMode.DRAGGING || actor != draggingCard) return;
        
        // Move card to follow cursor with slight interpolation for smoothness
        Vector2 local = handArea.stageToLocalCoordinates(new Vector2(stageX, stageY));
        float targetX = local.x - actor.getWidth() / 2f;
        float targetY = local.y - actor.getHeight() / 2f;
        
        // Smoothly interpolate current position to target (lerp)
        actor.setPosition(
            com.badlogic.gdx.math.MathUtils.lerp(actor.getX(), targetX, 0.4f),
            com.badlogic.gdx.math.MathUtils.lerp(actor.getY(), targetY, 0.4f)
        );
    }

    private void onCardDrop(CardActor actor, float stageX, float stageY) {
        if (inputMode != InputMode.DRAGGING || actor != draggingCard) {
            if (actor != null) actor.snapBack();
            return;
        }
        
        playerBoard.clearHighlights();
        actor.setLifted(false);
        
        boolean isUnit = actor.getCard().getTemplate().cardType() == CardType.UNIT;
        
        if (isUnit) {
            // Check if dropped on a valid board slot
            int slotIndex = playerBoard.getSlotIndexAt(stageX, stageY);
            if (slotIndex >= 0 && gameState.getBoard(0)[slotIndex] == null) {
                tryPlayCard(actor.getCard(), slotIndex, null);
                onMoveMade();
            } else {
                // Invalid drop — snap back
                actor.snapBack();
                if (hud != null) hud.setInputModeMessage("DRAG A CARD TO PLAY");
            }
        } else {
            // Non-unit cards (spells/items) — play immediately on drop anywhere reasonable
            // Check if dropped roughly on the board area
            if (stageY > 100) {
                tryPlayCard(actor.getCard(), -1, null);
                onMoveMade();
            } else {
                actor.snapBack();
                if (hud != null) hud.setInputModeMessage("DRAG A CARD TO PLAY");
            }
        }
        
        inputMode = InputMode.IDLE;
        draggingCard = null;
    }

    // ── Click handlers (sacrifice mode + board card clicks) ────────────────────

    private void onHandCardClicked(CardActor actor) {
        if (gameOver || gameState.getCurrentPlayer() != 0) return;
        
        if (inputMode == InputMode.SACRIFICE) {
            // Clicking a different hand card cancels sacrifice mode
            clearAllSelections();
            return;
        }
        
        // Card detail overlay has been removed to improve pacing.
    }

    private void onPlayerBoardCardClicked(CardActor actor) {
        if (gameOver || gameState.getCurrentPlayer() != 0) return;

        if (inputMode == InputMode.SACRIFICE && pendingSacrificeCard != null) {
            // Sacrifice this board card
            if (!sacrificeTargets.contains(actor.getCard())) {
                sacrificeTargets.add(actor.getCard());
                actor.setSelected(true);
                
                int needed = pendingSacrificeCard.getCard().getTemplate().bloodCost() 
                           - gameState.getPlayer(0).sacrificeCredit;
                if (sacrificeTargets.size() >= needed) {
                    // Execute sacrifice
                    try {
                        List<GameEvent> evts = new SacrificeAction(0, sacrificeTargets).execute(gameState);
                        gameState.pushEvents(evts);
                        
                        // Now the card can be played — check if it's a unit
                        CardInstance card = pendingSacrificeCard.getCard();
                        boolean isUnit = card.getTemplate().cardType() == CardType.UNIT;
                        
                        if (isUnit) {
                            // Find first empty slot and play there
                            int emptySlot = -1;
                            for (int i = 0; i < Constants.MAX_BOARD_SIZE; i++) {
                                if (gameState.getBoard(0)[i] == null) {
                                    emptySlot = i;
                                    break;
                                }
                            }
                            if (emptySlot >= 0) {
                                tryPlayCard(card, emptySlot, null);
                                onMoveMade();
                            }
                        } else {
                            tryPlayCard(card, -1, null);
                            onMoveMade();
                        }
                        
                        clearAllSelections();
                    } catch (IllegalStateException e) {
                        clearAllSelections();
                        if (hud != null) hud.setInputModeMessage(e.getMessage().toUpperCase());
                    }
                } else {
                    if (hud != null) hud.setInputModeMessage("SELECT " + (needed - sacrificeTargets.size()) + " MORE TO SACRIFICE");
                }
            }
        } else {
            clearAllSelections();
        }
    }

    private void onOpponentBoardCardClicked(CardActor actor) {
        // No direct interaction with opponent board cards in the new system
    }

    // ── Sacrifice mode ────────────────────────────────────────────────────────

    private void enterSacrificeMode(CardActor handCard) {
        inputMode = InputMode.SACRIFICE;
        pendingSacrificeCard = handCard;
        sacrificeTargets.clear();
        handCard.setSelected(true);
        handCard.snapBack(); // make sure it's not floating
        
        int needed = handCard.getCard().getTemplate().bloodCost() - gameState.getPlayer(0).sacrificeCredit;
        if (hud != null) hud.setInputModeMessage("SELECT " + needed + " MINIONS TO SACRIFICE");
    }

    // ── One-move-per-turn: auto advance ───────────────────────────────────────

    private void onMoveMade() {
        if (gameOver) return;
        clearAllSelections();
        List<GameEvent> events = turnManager.onMoveMade(gameState);
        gameState.pushEvents(events);
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
        inputMode = InputMode.IDLE;
        sacrificeTargets.clear();
        pendingSacrificeCard = null;
        draggingCard = null;
        
        playerBoard.clearSelection();
        playerBoard.clearHighlights();
        opponentBoard.clearSelection();
        handArea.clearSelection();
        
        if (hud != null) {
            hud.setInputModeMessage("DRAG A CARD TO PLAY");
        }
    }

    // ── Auto-pass check ───────────────────────────────────────────────────────

    /**
     * Check if the current player has any playable card. If not, auto-pass.
     */
    private boolean hasAnyPlayableCard(int playerIndex) {
        GameState.PlayerState ps = gameState.getPlayer(playerIndex);
        
        // Count board units (for sacrifice availability)
        int boardUnits = 0;
        for (CardInstance c : ps.board) {
            if (c != null && !c.isDead()) boardUnits++;
        }
        
        // Count empty board slots
        int emptySlots = 0;
        for (CardInstance c : ps.board) {
            if (c == null) emptySlots++;
        }
        
        for (CardInstance card : ps.hand) {
            int boneCost = card.getTemplate().boneCost();
            int bloodCost = card.getTemplate().bloodCost();
            boolean isUnit = card.getTemplate().cardType() == CardType.UNIT;
            
            // Check bone cost
            if (boneCost > ps.bones) continue;
            
            // Check blood cost — can be paid by sacrifice credit OR sacrificing board units
            int availableBlood = ps.sacrificeCredit + boardUnits;
            if (bloodCost > availableBlood) continue;
            
            // Units need an empty board slot
            if (isUnit) {
                // If the board is full, we can only play this unit if playing it forces
                // us to sacrifice a board unit (which will free up a slot).
                boolean forcesSacrifice = (bloodCost > ps.sacrificeCredit);
                if (emptySlots == 0 && !forcesSacrifice) {
                    continue; // No space, and playing this won't free a space.
                }
            }
            
            return true; // at least one card is playable
        }
        return false;
    }

    // ── Event handling ────────────────────────────────────────────────────────

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
            Gdx.app.log("BattleScreen", "Game over — " + winner);
            if (hud != null) hud.setInputModeMessage(winner + "!");
            onGameOver(goe.winnerIndex());
        } else if (event instanceof CardAttackedEvent cae) {
            CardActor actor = findCardActor(cae.attacker());
            if (actor != null) {
                // Lunge animation
                float dirY = (gameState.findBoardOwner(cae.attacker()) == 0) ? 30f : -30f;
                actor.addAction(Actions.sequence(
                    Actions.moveBy(0, dirY, 0.1f, Interpolation.pow2Out),
                    Actions.moveBy(0, -dirY, 0.15f, Interpolation.pow2In)
                ));
            }
        } else if (event instanceof DamageDealtEvent dde) {
            spawnFloatingText(dde.target(), "-" + dde.amount(), Color.RED);
            CardActor actor = findCardActor(dde.target());
            if (actor != null) {
                // Recoil animation
                actor.addAction(Actions.sequence(
                    Actions.moveBy(10, 0, 0.05f),
                    Actions.moveBy(-20, 0, 0.05f),
                    Actions.moveBy(10, 0, 0.05f)
                ));
            }
        } else if (event instanceof PlayerDamagedEvent pde) {
            // Floating damage text near the HP bar
            float yPos = (pde.playerIndex() == 0) ? 180f : Constants.VIEWPORT_HEIGHT - 60f;
            spawnFloatingText(Constants.VIEWPORT_WIDTH / 2f, yPos, "-" + pde.amount(), Color.RED);
        }
    }

    private void spawnFloatingText(CardInstance target, String text, Color color) {
        CardActor actor = findCardActor(target);
        if (actor != null) {
            Vector2 pos = actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2f, actor.getHeight() / 2f));
            spawnFloatingText(pos.x, pos.y, text, color);
        }
    }

    private void spawnFloatingText(float x, float y, String text, Color color) {
        if (stage == null) return;
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = new BitmapFont();
        style.font.getData().setScale(1.5f);
        style.fontColor = color;
        Label label = new Label(text, style);
        label.setPosition(x - label.getPrefWidth() / 2f, y);
        label.addAction(Actions.sequence(
            Actions.moveBy(0, 50, 1.0f, Interpolation.fade),
            Actions.fadeOut(0.2f),
            Actions.removeActor()
        ));
        stage.addActor(label);
    }

    private CardActor findCardActor(CardInstance card) {
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
        // Safely transition screen on the next frame to avoid disposing the stage mid-render
        Gdx.app.postRunnable(() -> game.setScreen(new GameOverScreen(game, winner)));
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
                // Execute Leshy's scripted queue turn
                List<GameEvent> events = turnManager.executeLeshyTurn(gameState);
                gameState.pushEvents(events);
                aiTimer = 1.0f;
            }
        }

        // Auto-pass for player: if it's player's turn and they have no playable cards
        if (!gameOver && gameState.getCurrentPlayer() == 0
                && turnManager.getCurrentPhase() == TurnManager.Phase.MAIN
                && inputMode == InputMode.IDLE) {
            if (!hasAnyPlayableCard(0)) {
                if (hud != null) hud.setInputModeMessage("NO PLAYABLE CARDS — PASSING...");
                // Auto-pass after a brief delay (use a simple timer approach)
                aiTimer -= delta;
                if (aiTimer <= 0) {
                    List<GameEvent> turnEvents = turnManager.endTurn(gameState);
                    gameState.pushEvents(turnEvents);
                    aiTimer = 1.0f;
                }
            } else {
                aiTimer = 1.0f; // reset timer when player has playable cards
            }
        }

        // Sync UI with game state
        opponentBoard.syncWithState(gameState);
        playerBoard.syncWithState(gameState);
        handArea.syncWithState(gameState);
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
        if (handArea  != null) { handArea.disposeAll(); }
        if (hud       != null) { hud.disposeResources(); }
        if (stage     != null) { stage.dispose(); stage = null; }
        if (bgTexture != null) { bgTexture.dispose(); }
        if (zoneDivider != null) { zoneDivider.dispose(); }
        if (playerDrawPile != null) { playerDrawPile.dispose(); }
        if (playerDiscardPile != null) { playerDiscardPile.dispose(); }
        if (font      != null) { font.dispose(); }
    }
}
