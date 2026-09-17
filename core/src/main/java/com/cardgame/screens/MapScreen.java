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
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.CardBattlerGame;
import com.cardgame.logic.RunManager;
import com.cardgame.logic.RunManager.MapNodeData;
import com.cardgame.ui.PauseOverlay;
import com.cardgame.ui.UiTheme;
import com.cardgame.ui.GameArt;
import com.cardgame.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapScreen implements Screen {

    private final CardBattlerGame game;
    private Stage stage;
    private Stage uiStage;
    private Texture bgTexture;
    
    private Texture combatTex, eliteTex, treasureTex, shopTex, bossTex;
    
    private Group mapContainer;
    private ScrollPane mapScroller;
    
    private Texture glowTexture;
    private Texture pathTexture;
    private TextureRegion pathRegion;
    private float stateTime;
    private static final float DOT_SPACING = 22f;
    private static final float DOT_SPEED = 25f;
    private static final Color ROUTE_AVAILABLE = Color.valueOf("185c55");
    private static final Color ROUTE_TRAVELED = Color.valueOf("87432e");
    private static final Color ROUTE_FUTURE = Color.valueOf("554536");
    private static final Color ROUTE_PAST = Color.valueOf("655342");
    private BitmapFont font;
    private BitmapFont smallFont;
    private BitmapFont tinyFont;

    public static float savedScrollPercentY = 1.0f;

    private PauseOverlay pauseOverlay;
    private boolean paused = false;

    // Visual coordinates are separate from persistent map anchors.
    private final Map<Integer, NodeVisual> nodeVisuals = new HashMap<>();
    private final List<PathVisual> paths = new ArrayList<>();

    private static final class NodeVisual {
        final MapNodeData node;
        final ImageButton button;
        final ParticleEffect particles;
        float x, y;
        boolean hovered;

        NodeVisual(MapNodeData node, ImageButton button, ParticleEffect particles) {
            this.node = node;
            this.button = button;
            this.particles = particles;
            x = node.x;
            y = node.y;
        }
    }

    private record PathVisual(NodeVisual from, NodeVisual to, boolean available,
                              boolean traversed, boolean past) {}

    public MapScreen(CardBattlerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stateTime = 0f;
        paused = false;
        stage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));
        uiStage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));

        try {
            bgTexture = new Texture(Gdx.files.internal("IMAGES/play/seamless_parchment.png"));
        } catch (Exception e) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(new Color(0.1f, 0.1f, 0.15f, 1f));
            pm.fill();
            bgTexture = new Texture(pm);
            bgTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            pm.dispose();
        }
        bgTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        bgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
        try { combatTex = new Texture(Gdx.files.internal("IMAGES/play/combatIcon.png")); } catch(Exception e) {}
        try { eliteTex = new Texture(Gdx.files.internal("IMAGES/play/eliteIcon.png")); } catch(Exception e) { eliteTex = combatTex; }
        try { treasureTex = new Texture(Gdx.files.internal("IMAGES/play/treasureIcon.png")); } catch(Exception e) { treasureTex = combatTex; }
        try { shopTex = new Texture(Gdx.files.internal("IMAGES/play/shopIcon.png")); } catch(Exception e) { shopTex = combatTex; }
        try { bossTex = new Texture(Gdx.files.internal("IMAGES/play/bossIcon.png")); } catch(Exception e) { bossTex = combatTex; }

        createGlowTexture();
        font = UiTheme.font(20f);
        smallFont = UiTheme.font(14f);
        tinyFont = UiTheme.font(13f);

        buildHUD();
        buildPauseOverlay();

        int mapWidth = (int) Constants.VIEWPORT_WIDTH;
        int mapHeight = 4000;
        
        mapContainer = new Group() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                float packedColor = batch.getPackedColor();
                applyTransform(batch, computeTransform());
                try {
                    drawPaths(batch, parentAlpha * getColor().a);
                    drawNodeParticles(batch);
                    drawChildren(batch, parentAlpha);
                } finally {
                    resetTransform(batch);
                    batch.setPackedColor(packedColor);
                }
            }
        };
        mapContainer.setSize(mapWidth, mapHeight);
        mapContainer.setTouchable(Touchable.childrenOnly);

        // Parchment stays in screen space; only its UVs track a fraction of scrolling.
        Actor mapBackground = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                float packedColor = batch.getPackedColor();
                float u = MathUtils.sin(stateTime * 0.08f) * 0.008f;
                float v = mapScroller.getVisualScrollY() * 0.18f / 768f
                        + MathUtils.cos(stateTime * 0.06f) * 0.006f;
                batch.setColor(0.65f, 0.61f, 0.57f, parentAlpha);
                batch.draw(bgTexture, 0f, 0f, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT,
                        u, v, u + Constants.VIEWPORT_WIDTH / 768f, v + Constants.VIEWPORT_HEIGHT / 768f);
                batch.setPackedColor(packedColor);
            }
        };
        mapBackground.setTouchable(Touchable.disabled);
        stage.addActor(mapBackground);
        
        buildMap(); // Add nodes to mapContainer
        
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        mapScroller = new ScrollPane(mapContainer, scrollStyle);
        mapScroller.setScrollingDisabled(true, false);
        mapScroller.setFillParent(true);
        mapScroller.setSmoothScrolling(true);
        stage.addActor(mapScroller);
        stage.setScrollFocus(mapScroller);

        InputMultiplexer multiplexer = new InputMultiplexer();
        InputAdapter escapeAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    togglePause();
                    return true;
                }
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (!paused && mapScroller != null) {
                    float step = 140f;
                    mapScroller.setScrollY(mapScroller.getScrollY() + amountY * step);
                    mapScroller.updateVisualScroll();
                    return true;
                }
                return false;
            }
        };
        
        multiplexer.addProcessor(escapeAdapter);
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);
        
        // Restore camera position
        mapScroller.layout();
        mapScroller.setScrollPercentY(savedScrollPercentY);
        mapScroller.updateVisualScroll();
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

        Table topBarContainer = new Table();
        topBarContainer.setFillParent(true);
        topBarContainer.top().left();

        Table topBar = new Table();
        topBar.setBackground(UiTheme.panel(new Color(0.055f, 0.06f, 0.075f, 0.97f),
                new Color(0.34f, 0.29f, 0.28f, 1f)));
        topBar.pad(14f, 28f, 14f, 28f);

        String charName = rm.getSelectedCharacter() != null ? rm.getSelectedCharacter().name() : "Player";
        Label hpLabel    = new Label(charName + " | HP: " + rm.getCurrentHp() + "/" + rm.getMaxHp(), new Label.LabelStyle(font, new Color(0.65f, 0.88f, 0.70f, 1f)));
        Label goldLabel  = new Label("Gold: " + rm.getGold(), new Label.LabelStyle(font, Color.GOLD));
        Label floorLabel = new Label("Floor: " + (rm.getCurrentNodeIndex() + 1), new Label.LabelStyle(font, Color.WHITE));

        TextButton.TextButtonStyle deckBtnStyle = UiTheme.button(smallFont);
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

        topBar.add(GameArt.image(GameArt.HEART)).size(30f).padRight(10f);
        topBar.add(hpLabel).padRight(32);
        topBar.add(GameArt.image(GameArt.GOLD)).size(30f).padRight(10f);
        topBar.add(goldLabel).padRight(32);
        topBar.add(floorLabel).expandX().left();
        topBar.add(deckBtn).height(44f);

        topBarContainer.add(topBar).expandX().fillX().row();
        Table legend = new Table();
        legend.setBackground(UiTheme.panel(Color.valueOf("dfcfb3"), Color.valueOf("a58c6a")));
        legend.pad(12f, 28f, 12f, 28f);
        legend.add(new Label("ASYLUM MAP", new Label.LabelStyle(tinyFont, ROUTE_FUTURE))).padRight(30f);
        legend.add(new Label("---  Available", new Label.LabelStyle(tinyFont, ROUTE_AVAILABLE))).padRight(26f);
        legend.add(new Label("___  Traveled", new Label.LabelStyle(tinyFont, ROUTE_TRAVELED))).padRight(26f);
        legend.add(new Label("---  Unexplored", new Label.LabelStyle(tinyFont, ROUTE_FUTURE))).expandX().left();
        legend.add(new Label("Scroll to explore", new Label.LabelStyle(tinyFont, ROUTE_FUTURE))).padRight(184f);
        topBarContainer.add(legend).expandX().fillX();
        uiStage.addActor(topBarContainer);
    }

    private void createGlowTexture() {
        Pixmap glow = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        glow.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                float radius = (float) Math.hypot(x - 15.5f, y - 15.5f) / 16f;
                float alpha = Math.max(0f, 1f - radius);
                glow.setColor(1f, 1f, 1f, alpha * alpha);
                glow.drawPixel(x, y);
            }
        }
        glowTexture = new Texture(glow);
        glowTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        glow.dispose();
        // Opaque geometry keeps routes readable on textured parchment.
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        pathTexture = new Texture(pixel);
        pathRegion = new TextureRegion(pathTexture);
        pixel.dispose();
    }

    private ParticleEffect createNodeParticles(float x, float y) {
        ParticleEmitter emitter = new ParticleEmitter();
        emitter.setName("Available room embers");
        emitter.setMaxParticleCount(40);
        emitter.setSprites(Array.with(new Sprite(glowTexture)));
        emitter.setContinuous(true);
        emitter.setAttached(false);
        emitter.setAdditive(true);
        emitter.getDuration().setLow(1000f);
        emitter.getEmission().setHigh(7f);
        emitter.getLife().setHigh(1200f, 2200f);
        emitter.getXOffsetValue().setActive(true);
        emitter.getXOffsetValue().setLow(-30f, 30f);
        emitter.getYOffsetValue().setActive(true);
        emitter.getYOffsetValue().setLow(-24f, 12f);
        emitter.getXScale().setHigh(6f, 14f);
        emitter.getXScale().setTimeline(new float[] {0f, 1f});
        emitter.getXScale().setScaling(new float[] {1f, 0.3f});
        emitter.getVelocity().setActive(true);
        emitter.getVelocity().setHigh(12f, 24f);
        emitter.getAngle().setActive(true);
        emitter.getAngle().setHigh(70f, 110f);
        emitter.getTint().setColors(new float[] {0.75f, 1f, 0.65f});
        emitter.getTransparency().setHigh(0.65f);
        emitter.getTransparency().setTimeline(new float[] {0f, 0.2f, 0.7f, 1f});
        emitter.getTransparency().setScaling(new float[] {0f, 1f, 0.6f, 0f});
        ParticleEffect effect = new ParticleEffect();
        effect.getEmitters().add(emitter);
        effect.setPosition(x, y);
        effect.start();
        // Prewarm once so newly available rooms already have a faint ambient glow.
        for (int i = 0; i < 20; i++) effect.update(0.05f);
        return effect;
    }

    private void updateMapEffects(float delta) {
        stateTime += delta;
        for (NodeVisual visual : nodeVisuals.values()) {
            float phase = visual.node.id * 0.5f;
            visual.x = visual.node.x + MathUtils.sin(stateTime * 1.5f + phase) * 4f;
            visual.y = visual.node.y + MathUtils.cos(stateTime * 1.2f + phase) * 3f;
            visual.button.setPosition(visual.x - visual.button.getWidth() / 2f,
                    visual.y - visual.button.getHeight() / 2f);
            if (visual.particles != null) {
                visual.particles.setPosition(visual.x, visual.y);
                visual.particles.update(delta);
            }
        }
    }

    private void setNodeHovered(NodeVisual visual, boolean hovered) {
        if (visual.hovered == hovered) return;
        visual.hovered = hovered;
        visual.button.clearActions();
        float scale = hovered ? 1.2f : 1f;
        visual.button.addAction(Actions.scaleTo(scale, scale, 0.1f, Interpolation.pow2Out));
        // Scaling is sampled each frame, so the rate changes without restarting live particles.
        visual.particles.getEmitters().first().getEmission().setScaling(new float[] {hovered ? 2.5f : 1f});
    }

    private void drawPaths(Batch batch, float parentAlpha) {
        float movement = (stateTime * DOT_SPEED) % DOT_SPACING;
        for (PathVisual path : paths) {
            float dx = path.to.x - path.from.x;
            float dy = path.to.y - path.from.y;
            float distance = (float) Math.hypot(dx, dy);
            float start = (path.from.button.getWidth() / 2f) * path.from.button.getScaleX() + 8f;
            float end = distance - (path.to.button.getWidth() / 2f) * path.to.button.getScaleX() - 8f;
            if (end <= start) continue;
            float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
            Color ink = path.available ? ROUTE_AVAILABLE : path.traversed ? ROUTE_TRAVELED
                    : path.past ? ROUTE_PAST : ROUTE_FUTURE;
            float width = path.available ? 5f : 4f;
            float alpha = path.past && !path.traversed ? 0.9f : 1f;
            if (path.traversed) {
                drawRouteSegment(batch, path, start, end - start, distance, angle, width, ink, parentAlpha);
                continue;
            }
            // Only available routes move. Solid traveled paths remain distinct without color.
            float offset = path.available ? movement : 0f;
            float first = start + offset - DOT_SPACING;
            for (float d = first; d < end; d += DOT_SPACING) {
                float segmentStart = Math.max(start, d);
                float segmentEnd = Math.min(end, d + 13f);
                if (segmentEnd > segmentStart) {
                    drawRouteSegment(batch, path, segmentStart, segmentEnd - segmentStart,
                            distance, angle, width, ink, alpha * parentAlpha);
                }
            }
        }
    }

    private void drawRouteSegment(Batch batch, PathVisual path, float start, float length,
                                  float distance, float angle, float width, Color ink, float alpha) {
        float x = path.from.x + (path.to.x - path.from.x) * start / distance;
        float y = path.from.y + (path.to.y - path.from.y) * start / distance;
        batch.setColor(ink.r, ink.g, ink.b, alpha);
        batch.draw(pathRegion, x, y - width / 2f, 0f, width / 2f, length, width, 1f, 1f, angle);
    }

    private void drawNodeParticles(Batch batch) {
        int source = batch.getBlendSrcFunc(), destination = batch.getBlendDstFunc();
        int sourceAlpha = batch.getBlendSrcFuncAlpha(), destinationAlpha = batch.getBlendDstFuncAlpha();
        float packedColor = batch.getPackedColor();
        try {
            batch.setColor(Color.WHITE);
            for (NodeVisual visual : nodeVisuals.values()) {
                if (visual.particles != null) visual.particles.draw(batch);
            }
        } finally {
            batch.setBlendFunctionSeparate(source, destination, sourceAlpha, destinationAlpha);
            batch.setPackedColor(packedColor);
        }
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
            boolean isReachable = reachableIds.contains(Integer.valueOf(node.id));
            boolean isDone = node.level <= visitedLevel;
            boolean isLastVisited = (node.id == lastVisitedId);

            Texture tex = combatTex;
            if (node.type.equals("ELITE")) tex = eliteTex;
            else if (node.type.equals("TREASURE")) tex = treasureTex;
            else if (node.type.equals("SHOP")) tex = shopTex;
            else if (node.type.equals("BOSS")) tex = bossTex;

            ImageButton.ImageButtonStyle imgStyle = new ImageButton.ImageButtonStyle();
            if (tex != null) {
                imgStyle.imageUp = new TextureRegionDrawable(node.type.equals("REST")
                        ? GameArt.icon(GameArt.REST) : new TextureRegion(tex));
            }
            ImageButton btn = new ImageButton(imgStyle);

            if (isLastVisited) {
                btn.setColor(Color.GOLD);
            } else if (isDone) {
                btn.setColor(Color.DARK_GRAY);
            } else if (isReachable) {
                btn.setColor(Color.WHITE);
            } else {
                btn.setColor(Color.GRAY);
            }

            float nodeSize = node.type.equals("BOSS") ? 84f : 64f;
            float halfSize = nodeSize / 2f;
            btn.setPosition(node.x - halfSize, node.y - halfSize);
            btn.setSize(nodeSize, nodeSize);
            btn.setOrigin(halfSize, halfSize);
            btn.setTransform(true);
            NodeVisual visual = new NodeVisual(node, btn,
                    isReachable && !isDone ? createNodeParticles(node.x, node.y) : null);
            nodeVisuals.put(node.id, visual);

            if (isReachable && !isDone) {
                btn.addListener(new InputListener() {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        if (pointer == -1 && !paused) setNodeHovered(visual, true);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        if (pointer == -1 && (toActor == null || !toActor.isDescendantOf(btn))) {
                            setNodeHovered(visual, false);
                        }
                    }
                });
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

            mapContainer.addActor(btn);
        }
        for (MapNodeData node : allNodes) {
            for (int nextId : node.nextNodeIds) {
                NodeVisual from = nodeVisuals.get(node.id);
                NodeVisual to = nodeVisuals.get(nextId);
                if (to != null) {
                    paths.add(new PathVisual(from, to,
                            node.id == lastVisitedId && to.particles != null,
                            rm.getPathTaken().contains(node.id) && rm.getPathTaken().contains(nextId),
                            node.level <= visitedLevel));
                }
            }
        }
        updateMapEffects(0f);
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

        if (!paused) {
            stage.act(delta);
            updateMapEffects(delta);
            uiStage.act(delta);
        } else {
            pauseOverlay.act(delta);
        }

        stage.getViewport().apply();
        stage.draw();
        uiStage.getViewport().apply();
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        uiStage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { 
        savedScrollPercentY = mapScroller.getScrollPercentY();
        dispose(); 
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (uiStage != null) uiStage.dispose();
        if (bgTexture != null) bgTexture.dispose();
        for (NodeVisual visual : nodeVisuals.values()) {
            if (visual.particles != null) visual.particles.dispose();
        }
        nodeVisuals.clear();
        paths.clear();
        if (glowTexture != null) glowTexture.dispose();
        if (pathTexture != null) pathTexture.dispose();
        // Missing room icons can share the combat texture fallback.
        Set<Texture> icons = new HashSet<>();
        icons.add(combatTex);
        icons.add(eliteTex);
        icons.add(treasureTex);
        icons.add(shopTex);
        icons.add(bossTex);
        for (Texture icon : icons) {
            if (icon != null) icon.dispose();
        }
        if (font != null) font.dispose();
        if (smallFont != null) smallFont.dispose();
        if (tinyFont != null) tinyFont.dispose();
        if (pauseOverlay != null) pauseOverlay.disposeResources();
    }
}
