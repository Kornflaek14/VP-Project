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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
    private Stage uiStage;
    private Texture bgTexture;
    
    private Texture combatTex, eliteTex, treasureTex, shopTex, restTex, bossTex;
    
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
        uiStage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));

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
        try { eliteTex = new Texture(Gdx.files.internal("IMAGES/play/monster1.png")); } catch(Exception e) { eliteTex = combatTex; }
        try { treasureTex = new Texture(Gdx.files.internal("IMAGES/play/treasureIcon.png")); } catch(Exception e) { treasureTex = combatTex; }
        try { shopTex = new Texture(Gdx.files.internal("IMAGES/play/shopIcon.png")); } catch(Exception e) { shopTex = combatTex; }
        try { restTex = new Texture(Gdx.files.internal("IMAGES/play/restIcon.png")); } catch(Exception e) { restTex = combatTex; }
        try { bossTex = new Texture(Gdx.files.internal("IMAGES/play/monster.png")); } catch(Exception e) { bossTex = combatTex; }

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
        InputAdapter inputAdapter = new InputAdapter() {
            private float dragStartY;
            private float camStartY;
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                dragStartY = screenY;
                camStartY = stage.getCamera().position.y;
                return false;
            }
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                float deltaY = screenY - dragStartY; // positive when dragging down
                float newY = camStartY + deltaY;
                newY = Math.max(Constants.VIEWPORT_HEIGHT / 2f, Math.min(4000f - Constants.VIEWPORT_HEIGHT / 2f, newY));
                stage.getCamera().position.y = newY;
                return true;
            }
            @Override
            public boolean scrolled(float amountX, float amountY) {
                float newY = stage.getCamera().position.y - amountY * 200f;
                newY = Math.max(Constants.VIEWPORT_HEIGHT / 2f, Math.min(4000f - Constants.VIEWPORT_HEIGHT / 2f, newY));
                stage.getCamera().position.y = newY;
                return true;
            }
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    togglePause();
                    return true;
                }
                return false;
            }
        };
        
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(inputAdapter);
        Gdx.input.setInputProcessor(multiplexer);
        
        // Start camera at the bottom (first nodes)
        stage.getCamera().position.y = Constants.VIEWPORT_HEIGHT / 2f;
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
        uiStage.addActor(pauseOverlay);
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

        uiStage.addActor(topBar);
    }

    private void buildMap() {
        RunManager rm = RunManager.getInstance();
        List<MapNodeData> allNodes = rm.getMapNodes();
        List<Integer> reachableIds = rm.getReachableNodeIds();
        int lastVisitedId = rm.getLastVisitedNodeId();
        
        if (lastVisitedId == -1 && !rm.getPathTaken().contains(-1)) {
            rm.getPathTaken().add(-1); // Mark start of path
        }

        MapNodeData lastVisited = rm.getNodeById(lastVisitedId);
        int visitedLevel = (lastVisited != null) ? lastVisited.level : -1;

        for (MapNodeData node : allNodes) {
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

            ImageButton.ImageButtonStyle imgStyle = new ImageButton.ImageButtonStyle();
            if (tex != null) {
                imgStyle.imageUp = new TextureRegionDrawable(new TextureRegion(tex));
            }
            ImageButton btn = new ImageButton(imgStyle);

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
                final MapNodeData clickedNode = node;
                btn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (paused) return;
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
        rm.getPathTaken().add(node.id);
        rm.advanceNode();

        if (node.room != null) {
            node.room.onPlayerEntry(game);
        } else {
            Gdx.app.error("MapScreen", "Node has no room assigned!");
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (bgTexture != null) {
            Batch batch = stage.getBatch();
            batch.setProjectionMatrix(stage.getCamera().combined);
            batch.begin();
            
            // Tile the background vertically to avoid stretching
            float bgHeight = (Constants.VIEWPORT_WIDTH / (float) bgTexture.getWidth()) * bgTexture.getHeight();
            for (float y = 0; y < 4500f; y += bgHeight) {
                batch.draw(bgTexture, 0, y, Constants.VIEWPORT_WIDTH, bgHeight);
            }
            batch.end();
        }

        RunManager rm = RunManager.getInstance();
        List<MapNodeData> allNodes = rm.getMapNodes();
        List<Integer> reachableIds = rm.getReachableNodeIds();
        int lastVisitedId = rm.getLastVisitedNodeId();
        List<Integer> pathTaken = rm.getPathTaken();

        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (MapNodeData node : allNodes) {
            for (int nextId : node.nextNodeIds) {
                float[] from = nodePositions.get(node.id);
                float[] to = nodePositions.get(nextId);
                if (from != null && to != null) {
                    if (pathTaken.contains(node.id) && pathTaken.contains(nextId)) {
                        shapeRenderer.setColor(Color.GOLD);
                        Gdx.gl.glLineWidth(4f);
                    } else if (node.id == lastVisitedId && reachableIds.contains(nextId)) {
                        shapeRenderer.setColor(new Color(0.96f, 0.84f, 0.38f, 1f));
                        Gdx.gl.glLineWidth(2f);
                    } else if (node.level <= (rm.getNodeById(lastVisitedId) != null ? rm.getNodeById(lastVisitedId).level : -1)) {
                        shapeRenderer.setColor(new Color(0.2f, 0.2f, 0.25f, 0.5f));
                        Gdx.gl.glLineWidth(2f);
                    } else {
                        shapeRenderer.setColor(new Color(0.3f, 0.3f, 0.4f, 0.6f));
                        Gdx.gl.glLineWidth(2f);
                    }
                    shapeRenderer.line(from[0], from[1], to[0], to[1]);
                }
            }
        }
        shapeRenderer.end();

        stage.act(delta);
        uiStage.act(delta);
        
        stage.draw();
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        uiStage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (uiStage != null) uiStage.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (combatTex != null) combatTex.dispose();
        if (eliteTex != null) eliteTex.dispose();
        if (treasureTex != null) treasureTex.dispose();
        if (shopTex != null) shopTex.dispose();
        if (restTex != null) restTex.dispose();
        if (bossTex != null) bossTex.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (smallFont != null) smallFont.dispose();
        if (tinyFont != null) tinyFont.dispose();
        if (pauseOverlay != null) pauseOverlay.disposeResources();
    }
}
