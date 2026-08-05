package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.logic.RunManager;
import com.cardgame.logic.RunManager.MapNodeData;
import com.cardgame.ui.PauseOverlay;
import com.cardgame.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private Texture bgTexture;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont smallFont;
    private BitmapFont tinyFont;

    private PauseOverlay pauseOverlay;
    private boolean paused = false;

    // Map from node ID to its position for drawing connections
    private final Map<Integer, float[]> nodePositions = new HashMap<>();

    public MapScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));

        try {
            bgTexture = new Texture(Gdx.files.internal("IMAGES/play/mapBg.jpg"));
        } catch (Exception e) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(new Color(0.1f, 0.1f, 0.15f, 1f));
            pm.fill();
            bgTexture = new Texture(pm);
            pm.dispose();
        }

        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.0f);
        tinyFont = new BitmapFont();
        tinyFont.getData().setScale(0.8f);

        buildMap();
        buildHUD();
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

    private void buildHUD() {
        RunManager rm = RunManager.getInstance();

        Table topBar = new Table();
        topBar.setFillParent(true);
        topBar.top().left().pad(20);

        String charName = rm.getSelectedCharacter() != null ? rm.getSelectedCharacter().name() : "Player";
        Label hpLabel    = new Label(charName + " | HP: " + rm.getCurrentHp() + "/" + rm.getMaxHp(), new Label.LabelStyle(font, Color.GREEN));
        Label goldLabel  = new Label("Gold: " + rm.getGold(), new Label.LabelStyle(font, Color.GOLD));
        Label floorLabel = new Label("Floor: " + (rm.getCurrentNodeIndex() + 1), new Label.LabelStyle(font, Color.WHITE));

        TextButton.TextButtonStyle deckBtnStyle = new TextButton.TextButtonStyle();
        deckBtnStyle.font = smallFont;
        deckBtnStyle.fontColor = new Color(0.7f, 0.85f, 1f, 1f);
        deckBtnStyle.overFontColor = Color.WHITE;

        TextButton deckBtn = new TextButton("VIEW DECK (" + rm.getDeck().size() + ")", deckBtnStyle);
        deckBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (paused) return;
                game.setScreen(new DeckViewerScreen(game, new MapScreen(game)));
            }
        });

        topBar.add(hpLabel).padRight(40);
        topBar.add(goldLabel).padRight(40);
        topBar.add(floorLabel).padRight(60);
        topBar.add(deckBtn);

        stage.addActor(topBar);
    }

    private void buildMap() {
        RunManager rm = RunManager.getInstance();
        List<MapNodeData> allNodes = rm.getMapNodes();
        List<Integer> reachableIds = rm.getReachableNodeIds();
        int lastVisitedId = rm.getLastVisitedNodeId();

        // Debug logging
        Gdx.app.log("MapScreen", "Building map. lastVisitedId=" + lastVisitedId
            + ", reachableIds=" + reachableIds + ", totalNodes=" + allNodes.size());

        // Determine visited level for "done" coloring
        MapNodeData lastVisited = rm.getNodeById(lastVisitedId);
        int visitedLevel = (lastVisited != null) ? lastVisited.level : -1;

        Gdx.app.log("MapScreen", "visitedLevel=" + visitedLevel);

        // ── Button styles ─────────────────────────────────────

        // DONE nodes: dark gray, disabled
        TextButton.TextButtonStyle doneStyle = new TextButton.TextButtonStyle();
        doneStyle.font = tinyFont;
        doneStyle.fontColor = new Color(0.3f, 0.3f, 0.3f, 1f);

        // REACHABLE nodes: bright green, clickable
        TextButton.TextButtonStyle reachableStyle = new TextButton.TextButtonStyle();
        reachableStyle.font = smallFont;
        reachableStyle.fontColor = new Color(0.3f, 1f, 0.3f, 1f);
        reachableStyle.overFontColor = Color.YELLOW;

        // LOCKED nodes: dim, disabled
        TextButton.TextButtonStyle lockedStyle = new TextButton.TextButtonStyle();
        lockedStyle.font = tinyFont;
        lockedStyle.fontColor = new Color(0.25f, 0.25f, 0.3f, 1f);

        // CURRENT node (last visited): gold
        TextButton.TextButtonStyle currentStyle = new TextButton.TextButtonStyle();
        currentStyle.font = smallFont;
        currentStyle.fontColor = new Color(0.96f, 0.84f, 0.38f, 1f);

        for (MapNodeData node : allNodes) {
            // Store position for line drawing
            nodePositions.put(node.id, new float[]{node.x, node.y});

            boolean isReachable = reachableIds.contains(Integer.valueOf(node.id));
            boolean isDone = node.level <= visitedLevel;
            boolean isLastVisited = (node.id == lastVisitedId);

            // Determine style and label
            TextButton.TextButtonStyle style;
            String label;

            if (isLastVisited) {
                style = currentStyle;
                label = ">> " + node.type + " <<";
            } else if (isDone) {
                style = doneStyle;
                label = "---";
            } else if (isReachable) {
                style = reachableStyle;
                label = "[ " + node.type + " ]";
            } else {
                style = lockedStyle;
                label = node.type;
            }

            TextButton btn = new TextButton(label, style);
            btn.setPosition(node.x - 55f, node.y - 20f);
            btn.setSize(110f, 40f);

            if (isReachable && !isDone) {
                // Only reachable, non-done nodes are clickable
                final MapNodeData clickedNode = node;
                btn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (paused) return;
                        Gdx.app.log("MapScreen", "Node clicked: id=" + clickedNode.id
                            + " level=" + clickedNode.level + " type=" + clickedNode.type);
                        handleNodeClick(clickedNode);
                    }
                });
            } else {
                btn.setDisabled(true);
            }

            stage.addActor(btn);
        }
    }

    private void handleNodeClick(MapNodeData node) {
        RunManager rm = RunManager.getInstance();
        rm.setLastVisitedNodeId(node.id);
        rm.advanceNode();

        Gdx.app.log("MapScreen", "Navigating to node " + node.id + " type=" + node.type
            + ". Next reachable: " + rm.getReachableNodeIds());

        switch (node.type) {
            case "COMBAT":
            case "ELITE":
            case "BOSS":
                game.setScreen(new BattleScreen(game));
                break;
            case "REST":
                game.setScreen(new RestScreen(game));
                break;
            case "SHOP":
                game.setScreen(new ShopScreen(game));
                break;
            case "TREASURE":
                game.setScreen(new TreasureScreen(game));
                break;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (bgTexture != null) {
            Batch batch = stage.getBatch();
            batch.begin();
            batch.draw(bgTexture, 0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
            batch.end();
        }

        // Draw connection lines
        RunManager rm = RunManager.getInstance();
        List<MapNodeData> allNodes = rm.getMapNodes();
        List<Integer> reachableIds = rm.getReachableNodeIds();
        int lastVisitedId = rm.getLastVisitedNodeId();

        Gdx.gl.glLineWidth(2f);
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (MapNodeData node : allNodes) {
            for (int nextId : node.nextNodeIds) {
                float[] from = nodePositions.get(node.id);
                float[] to = nodePositions.get(nextId);
                if (from != null && to != null) {
                    if (node.id == lastVisitedId && reachableIds.contains(Integer.valueOf(nextId))) {
                        // Gold highlight for paths from current node to reachable nodes
                        shapeRenderer.setColor(new Color(0.96f, 0.84f, 0.38f, 1f));
                    } else if (node.level <= (rm.getNodeById(lastVisitedId) != null ? rm.getNodeById(lastVisitedId).level : -1)) {
                        // Dim for already-passed paths
                        shapeRenderer.setColor(new Color(0.2f, 0.2f, 0.25f, 0.5f));
                    } else {
                        // Default dim for future paths
                        shapeRenderer.setColor(new Color(0.3f, 0.3f, 0.4f, 0.6f));
                    }
                    shapeRenderer.line(from[0], from[1], to[0], to[1]);
                }
            }
        }
        shapeRenderer.end();

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
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (smallFont != null) smallFont.dispose();
        if (tinyFont != null) tinyFont.dispose();
        if (pauseOverlay != null) pauseOverlay.disposeResources();
    }
}
