package com.cardgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.cardgame.logic.RunManager;
import com.cardgame.logic.RunManager.MapNodeData;
import com.cardgame.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/** Read-only map inspection that keeps the active BattleScreen and GameState intact. */
public final class CombatMapOverlay extends Group {
    private final BitmapFont font = UiTheme.font(18f);
    private final Texture lineTexture;
    private final Texture nodeDisc;
    private final Map<String, TextureRegion> icons = new HashMap<>();
    private final Drawable background = UiTheme.panel(new Color(0.025f, 0.035f, 0.05f, 0.98f), Color.DARK_GRAY);
    private final ScrollPane scroller;
    private final float mapHeight;

    public CombatMapOverlay() {
        setSize(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        setVisible(false);
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        lineTexture = new Texture(pixel);
        pixel.dispose();
        Pixmap disc = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        disc.setColor(Color.WHITE);
        disc.fillCircle(32, 32, 31);
        nodeDisc = new Texture(disc);
        nodeDisc.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        disc.dispose();
        loadIcon("COMBAT", "combatIcon.png");
        loadIcon("ELITE", "eliteIcon.png");
        loadIcon("TREASURE", "treasureIcon.png");
        loadIcon("SHOP", "shopIcon.png");
        loadIcon("REST", "restIcon.png");
        loadIcon("BOSS", "monster.png");
        float highest = 0f;
        for (MapNodeData node : RunManager.getInstance().getMapNodes()) highest = Math.max(highest, node.y);
        mapHeight = highest + 160f;
        Actor canvas = new Actor() {
            @Override public void draw(Batch batch, float parentAlpha) { drawMap(batch, getX(), getY(), parentAlpha); }
        };
        canvas.setSize(Constants.VIEWPORT_WIDTH, mapHeight);
        scroller = new ScrollPane(canvas);
        scroller.setScrollingDisabled(true, false);
        Table root = new Table();
        root.setFillParent(true);
        root.pad(24f);
        root.add(new Label("ASYLUM MAP  /  CURRENT ROOM IN GOLD", new Label.LabelStyle(font, Color.GOLD))).expandX().left();
        TextButton close = new TextButton("BACK TO BATTLE", UiTheme.button(font));
        close.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { hide(); }
        });
        root.add(close).size(240f, 44f).row();
        root.add(scroller).colspan(2).expand().fill().padTop(16f);
        addActor(root);
        addListener(new InputListener() {
            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; }
        });
    }

    private void loadIcon(String type, String filename) {
        Texture texture = new Texture(Gdx.files.internal("IMAGES/play/" + filename));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        icons.put(type, new TextureRegion(texture));
    }

    public void show() {
        setVisible(true);
        toFront();
        ((Table) getChildren().first()).validate();
        scroller.layout();
        MapNodeData current = RunManager.getInstance().getNodeById(RunManager.getInstance().getLastVisitedNodeId());
        scroller.setScrollY(mapHeight - (current == null ? 0f : current.y) - scroller.getHeight() / 2f);
        scroller.updateVisualScroll();
        getStage().setScrollFocus(scroller);
    }

    public void hide() {
        setVisible(false);
        if (getStage() != null && getStage().getScrollFocus() == scroller) getStage().setScrollFocus(null);
    }

    private void drawMap(Batch batch, float offsetX, float offsetY, float alpha) {
        RunManager rm = RunManager.getInstance();
        float previousColor = batch.getPackedColor();
        for (MapNodeData node : rm.getMapNodes()) {
            for (int nextId : node.nextNodeIds) {
                MapNodeData next = rm.getNodeById(nextId);
                if (next == null) continue;
                boolean visited = rm.getPathTaken().contains(node.id) && rm.getPathTaken().contains(next.id);
                batch.setColor(visited ? 0.95f : 0.3f, visited ? 0.72f : 0.37f, visited ? 0.3f : 0.42f, alpha);
                float dx = next.x - node.x, dy = next.y - node.y;
                float distance = (float) Math.hypot(dx, dy);
                batch.draw(lineTexture, offsetX + node.x, offsetY + node.y, 0f, 1f, distance, 2f,
                        1f, 1f, MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees, 0, 0, 1, 1, false, false);
            }
        }
        for (MapNodeData node : rm.getMapNodes()) {
            if (node.id == rm.getLastVisitedNodeId()) batch.setColor(1f, 0.8f, 0.3f, alpha);
            else if (rm.getPathTaken().contains(node.id)) batch.setColor(0.5f, 0.5f, 0.5f, alpha);
            else batch.setColor(0.73f, 0.76f, 0.72f, alpha);
            batch.draw(nodeDisc, offsetX + node.x - 34f, offsetY + node.y - 34f, 68f, 68f);
            batch.setColor(1f, 1f, 1f, alpha);
            CombatUiAssets.drawFitted(batch, icons.getOrDefault(node.type, icons.get("COMBAT")),
                    offsetX + node.x - 25f, offsetY + node.y - 25f, 50f, 50f);
        }
        batch.setPackedColor(previousColor);
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        if (!isVisible()) return;
        batch.setColor(1f, 1f, 1f, parentAlpha);
        background.draw(batch, 0f, 0f, getWidth(), getHeight());
        super.draw(batch, parentAlpha);
    }

    public void disposeResources() {
        font.dispose();
        lineTexture.dispose();
        nodeDisc.dispose();
        for (TextureRegion icon : icons.values()) icon.getTexture().dispose();
        icons.clear();
    }
}
