package com.cardgame.screens;

import com.badlogic.gdx.Gdx;
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
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private Texture bgTexture;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    
    private final List<MapNode> nodes = new ArrayList<>();
    private final Random rand = new Random();

    enum NodeType { COMBAT, ELITE, REST, TREASURE, SHOP, BOSS }

    class MapNode {
        float x, y;
        int level;
        NodeType type;
        List<MapNode> nextNodes = new ArrayList<>();
        TextButton btn;

        public MapNode(float x, float y, int level, NodeType type) {
            this.x = x;
            this.y = y;
            this.level = level;
            this.type = type;
        }
    }

    public MapScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        Gdx.input.setInputProcessor(stage);

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

        buildMap();
        buildHUD();
    }

    private void buildHUD() {
        RunManager rm = RunManager.getInstance();
        
        Table topBar = new Table();
        topBar.setFillParent(true);
        topBar.top().left().pad(20);

        String charName = rm.getSelectedCharacter() != null ? rm.getSelectedCharacter().name() : "Player";
        Label hpLabel = new Label(charName + " | HP: " + rm.getCurrentHp() + "/" + rm.getMaxHp(), new Label.LabelStyle(font, Color.GREEN));
        Label goldLabel = new Label("Gold: " + rm.getGold(), new Label.LabelStyle(font, Color.GOLD));
        Label floorLabel = new Label("Floor: " + (rm.getCurrentNodeIndex() + 1), new Label.LabelStyle(font, Color.WHITE));

        topBar.add(hpLabel).padRight(40);
        topBar.add(goldLabel).padRight(40);
        topBar.add(floorLabel);
        
        stage.addActor(topBar);
    }

    private void buildMap() {
        int maxLevels = RunManager.getInstance().getMaxNodes();
        int currentLevel = RunManager.getInstance().getCurrentNodeIndex();

        float startY = 100f;
        float endY = Constants.VIEWPORT_HEIGHT - 100f;
        float spacingY = (endY - startY) / (maxLevels - 1);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.GRAY;
        btnStyle.overFontColor = Color.WHITE;

        TextButton.TextButtonStyle activeStyle = new TextButton.TextButtonStyle(btnStyle);
        activeStyle.fontColor = Color.WHITE;
        activeStyle.overFontColor = Color.YELLOW;

        List<MapNode> prevLevelNodes = new ArrayList<>();

        for (int l = 0; l < maxLevels; l++) {
            int nodesInLevel = (l == 0 || l == maxLevels - 1) ? 1 : 2 + rand.nextInt(3);
            float spacingX = Constants.VIEWPORT_WIDTH / (nodesInLevel + 1f);

            List<MapNode> currentLevelNodes = new ArrayList<>();
            for (int i = 0; i < nodesInLevel; i++) {
                float x = spacingX * (i + 1);
                float y = startY + l * spacingY;

                NodeType type = determineNodeType(l, maxLevels);
                MapNode node = new MapNode(x, y, l, type);
                nodes.add(node);
                currentLevelNodes.add(node);

                TextButton btn = new TextButton(type.name(), l == currentLevel ? activeStyle : btnStyle);
                btn.setPosition(x - 50f, y - 20f);
                btn.setSize(100f, 40f);
                node.btn = btn;

                if (l < currentLevel) {
                    btn.setText("DONE");
                    btn.setDisabled(true);
                } else if (l > currentLevel) {
                    btn.setDisabled(true);
                } else {
                    btn.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            handleNodeClick(node.type);
                        }
                    });
                }
                stage.addActor(btn);
            }

            if (!prevLevelNodes.isEmpty()) {
                for (int i = 0; i < prevLevelNodes.size(); i++) {
                    MapNode prev = prevLevelNodes.get(i);
                    int targetIndex = (i * currentLevelNodes.size()) / prevLevelNodes.size();
                    prev.nextNodes.add(currentLevelNodes.get(targetIndex));
                    
                    if (rand.nextBoolean() && targetIndex + 1 < currentLevelNodes.size()) {
                        prev.nextNodes.add(currentLevelNodes.get(targetIndex + 1));
                    }
                }
                for (int i = 0; i < currentLevelNodes.size(); i++) {
                    MapNode curr = currentLevelNodes.get(i);
                    boolean hasIncoming = false;
                    for (MapNode prev : prevLevelNodes) {
                        if (prev.nextNodes.contains(curr)) {
                            hasIncoming = true;
                            break;
                        }
                    }
                    if (!hasIncoming) {
                        MapNode randomPrev = prevLevelNodes.get(rand.nextInt(prevLevelNodes.size()));
                        if (!randomPrev.nextNodes.contains(curr)) {
                            randomPrev.nextNodes.add(curr);
                        }
                    }
                }
            }
            prevLevelNodes = currentLevelNodes;
        }
    }

    private NodeType determineNodeType(int level, int maxLevels) {
        if (level == maxLevels - 1) return NodeType.BOSS;
        if (level == 0) return NodeType.COMBAT;
        
        int roll = rand.nextInt(100);
        if (roll < 40) return NodeType.COMBAT;
        if (roll < 60) return NodeType.ELITE;
        if (roll < 75) return NodeType.REST;
        if (roll < 90) return NodeType.SHOP;
        return NodeType.TREASURE;
    }

    private void handleNodeClick(NodeType type) {
        RunManager.getInstance().advanceNode();
        switch (type) {
            case COMBAT:
            case ELITE:
            case BOSS:
                game.setScreen(new BattleScreen(game));
                break;
            case REST:
                game.setScreen(new RestScreen(game));
                break;
            case SHOP:
                game.setScreen(new ShopScreen(game));
                break;
            case TREASURE:
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

        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(0.4f, 0.4f, 0.5f, 1f));
        for (MapNode node : nodes) {
            for (MapNode next : node.nextNodes) {
                shapeRenderer.line(node.x, node.y, next.x, next.y);
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
    }
}
