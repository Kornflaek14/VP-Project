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
    private Texture combatTex, eliteTex, treasureTex, shopTex, restTex, bossTex, barTex;

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
        
        try { combatTex = new Texture(Gdx.files.internal("IMAGES/play/combatIcon.png")); } catch(Exception e) {}
        try { eliteTex = new Texture(Gdx.files.internal("IMAGES/play/eliteIcon.png")); } catch(Exception e) { eliteTex = combatTex; }
        try { treasureTex = new Texture(Gdx.files.internal("IMAGES/play/treasureIcon.png")); } catch(Exception e) { treasureTex = combatTex; }
        try { shopTex = new Texture(Gdx.files.internal("IMAGES/play/shopIcon.png")); } catch(Exception e) { shopTex = combatTex; }
        try { restTex = new Texture(Gdx.files.internal("IMAGES/play/restIcon.png")); } catch(Exception e) { restTex = combatTex; }
        try { bossTex = new Texture(Gdx.files.internal("IMAGES/play/monster.png")); } catch(Exception e) { bossTex = combatTex; }
        try { barTex = new Texture(Gdx.files.internal("IMAGES/play/bar.png")); } catch(Exception e) {}

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

        Table topBarContainer = new Table();
        topBarContainer.setFillParent(true);
        topBarContainer.top().left();

        Table topBar = new Table();
        if (barTex != null) {
            topBar.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(barTex)));
        }
        topBar.pad(20);

        String charName = rm.getSelectedCharacter() != null ? rm.getSelectedCharacter().name() : "Player";
        Label hpLabel = new Label(charName + " | HP: " + rm.getCurrentHp() + "/" + rm.getMaxHp(), new Label.LabelStyle(font, Color.GREEN));
        Label goldLabel = new Label("Gold: " + rm.getGold(), new Label.LabelStyle(font, Color.GOLD));
        Label floorLabel = new Label("Floor: " + (rm.getCurrentNodeIndex() + 1), new Label.LabelStyle(font, Color.WHITE));

        topBar.add(hpLabel).padRight(40);
        topBar.add(goldLabel).padRight(40);
        topBar.add(floorLabel);

        topBarContainer.add(topBar).expandX().fillX();
        stage.addActor(topBarContainer);
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

        for (MapNodeData node : allNodes) {
            // Store position for line drawing
            nodePositions.put(node.id, new float[]{node.x, node.y});

            boolean isReachable = reachableIds.contains(Integer.valueOf(node.id));
            boolean isDone = node.level <= visitedLevel;
            boolean isLastVisited = (node.id == lastVisitedId);

            Texture tex = combatTex;
            if (node.type.equals("ELITE")) tex = eliteTex;
            else if (node.type.equals("TREASURE")) tex = treasureTex;
            else if (node.type.equals("SHOP")) tex = shopTex;
            else if (node.type.equals("REST")) tex = restTex;
            else if (node.type.equals("BOSS")) tex = bossTex;

            com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle imgStyle = new com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle();
            if (tex != null) {
                imgStyle.imageUp = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(tex));
            }
            com.badlogic.gdx.scenes.scene2d.ui.ImageButton btn = new com.badlogic.gdx.scenes.scene2d.ui.ImageButton(imgStyle);

            if (isLastVisited) {
                btn.setColor(Color.GOLD);
            } else if (isDone) {
                btn.setColor(Color.DARK_GRAY);
            } else if (isReachable) {
                btn.setColor(Color.GREEN);
            } else {
                btn.setColor(Color.GRAY);
            }

            btn.setPosition(node.x - 32f, node.y - 32f);
            btn.setSize(64f, 64f);

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
        if (combatTex != null) combatTex.dispose();
        if (eliteTex != null) eliteTex.dispose();
        if (treasureTex != null) treasureTex.dispose();
        if (shopTex != null) shopTex.dispose();
        if (restTex != null) restTex.dispose();
        if (bossTex != null) bossTex.dispose();
        if (barTex != null) barTex.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (smallFont != null) smallFont.dispose();
        if (tinyFont != null) tinyFont.dispose();
        if (pauseOverlay != null) pauseOverlay.disposeResources();
    }
}
