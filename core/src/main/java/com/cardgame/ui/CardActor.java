package com.cardgame.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.utils.Constants;

import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;

/**
 * Scene2D Actor for rendering a single card in STS style.
 *
 * On hover: lifts card 20px, scales slightly, and shows a full
 * CardPreviewOverlay centered on screen.
 */
public class CardActor extends Actor {

    public interface OnClickCallback { void onClick(CardActor actor); }

    public interface OnDragCallback { 
        void onDragStart(CardActor actor);
        void onDrag(CardActor actor, float x, float y);
        void onDragStop(CardActor actor, float x, float y);
    }

    private final AbstractCard card;
    private boolean hovered = false;
    private boolean dragging = false;

    public final Vector2 targetPos = new Vector2();
    public float targetScale = 1f;
    public float targetRot = 0f;
    
    public boolean isDragging() { return dragging; }

    // Textures
    private Texture cardImage;
    private Texture borderTex;
    private Texture costBg;
    private Texture statBg;

    private final BitmapFont font;
    private final BitmapFont smallFont;

    // Color coding by card type
    private static final Color ATTACK_COLOR = new Color(0.85f, 0.25f, 0.20f, 1f);
    private static final Color SKILL_COLOR  = new Color(0.20f, 0.50f, 0.85f, 1f);
    private static final Color POWER_COLOR  = new Color(0.85f, 0.75f, 0.20f, 1f);

    /** Shared texture cache across all card actors. */
    private static final Map<String, Texture> imageCache = new HashMap<>();

    /** The active preview overlay — set by BattleScreen. */
    private static CardPreviewOverlay previewOverlay = null;

    public static void setPreviewOverlay(CardPreviewOverlay overlay) {
        previewOverlay = overlay;
    }

    /** Returns a cached texture for the given path (used by CardPreviewOverlay). */
    public static Texture getCachedTexture(String path) {
        return imageCache.get(path);
    }

    public CardActor(AbstractCard card, OnClickCallback clickCallback) {
        this(card);
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (clickCallback != null) clickCallback.onClick(CardActor.this);
            }
        });
    }

    public CardActor(AbstractCard card, OnDragCallback dragCallback) {
        this(card);
        addListener(new DragListener() {
            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                dragging = true;
                if (previewOverlay != null) previewOverlay.hide();
                toFront();
                if (dragCallback != null) dragCallback.onDragStart(CardActor.this);
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                Vector2 mouseCoords = getStage().screenToStageCoordinates(
                    new Vector2(Gdx.input.getX(), Gdx.input.getY())
                );
                targetPos.set(mouseCoords.x - (getWidth() / 2), mouseCoords.y - (getHeight() / 2));
                targetScale = 1f;
                targetRot = 0f;
                if (dragCallback != null) dragCallback.onDrag(CardActor.this, mouseCoords.x, mouseCoords.y);
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                dragging = false;
                Vector2 mouseCoords = getStage().screenToStageCoordinates(
                    new Vector2(Gdx.input.getX(), Gdx.input.getY())
                );
                if (dragCallback != null) dragCallback.onDragStop(CardActor.this, mouseCoords.x, mouseCoords.y);
            }
        });
    }

    public CardActor(AbstractCard card) {
        this.card = card;
        this.font = new BitmapFont();
        this.font.getData().setScale(0.9f);
        this.smallFont = new BitmapFont();
        this.smallFont.getData().setScale(0.7f);
        
        // Default size and origin
        setSize(Constants.CARD_WIDTH, Constants.CARD_HEIGHT);
        setOrigin(getWidth() / 2f, getHeight() / 2f);

        buildTextures();

        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (dragging) return;
                hovered = true;
                targetScale = 1.2f;
                targetRot = 0f;
                targetPos.y += 40f;
                toFront();
                if (previewOverlay != null) previewOverlay.show(CardActor.this);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (dragging) return;
                hovered = false;
                targetScale = 1f;
                targetPos.y -= 40f;
                if (previewOverlay != null) previewOverlay.hide();
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        setX(MathUtils.lerp(getX(), targetPos.x, 15f * delta));
        setY(MathUtils.lerp(getY(), targetPos.y, 15f * delta));
        setScale(MathUtils.lerp(getScaleX(), targetScale, 15f * delta));
        setRotation(MathUtils.lerp(getRotation(), targetRot, 15f * delta));
    }

    private void buildTextures() {
        Color typeColor;
        switch (card.cardType()) {
            case SKILL:  typeColor = SKILL_COLOR;  break;
            case POWER:  typeColor = POWER_COLOR;  break;
            default:     typeColor = ATTACK_COLOR; break;
        }
        borderTex = singlePixel(typeColor);
        costBg    = singlePixel(new Color(0.1f, 0.1f, 0.1f, 0.85f));
        statBg    = singlePixel(new Color(0.05f, 0.05f, 0.08f, 0.75f));

        // Load card image from assets (shared cache)
        String imagePath = card.image();
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imageCache.containsKey(imagePath)) {
                cardImage = imageCache.get(imagePath);
            } else {
                try {
                    if (Gdx.files.internal(imagePath).exists()) {
                        cardImage = new Texture(Gdx.files.internal(imagePath));
                        cardImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                        imageCache.put(imagePath, cardImage);
                    }
                } catch (Exception e) {
                    cardImage = null;
                }
            }
        }
    }

    private static Texture singlePixel(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    public AbstractCard getCard() { return card; }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float w = getWidth();
        float h = getHeight();
        float x = getX();
        float y = getY();
        float alpha = parentAlpha * getColor().a;
        float scaleX = getScaleX();
        float scaleY = getScaleY();
        float rotation = getRotation();
        float originX = getOriginX();
        float originY = getOriginY();

        // Glow/border (brighter and cyan on hover)
        float bw = hovered || dragging ? 6f : 3f;
        
        if (hovered && !dragging) {
            // Slay the spire style bright blue/cyan glow
            batch.setColor(0.2f, 0.8f, 1f, 0.9f * alpha);
            batch.draw(statBg, x - bw, y - bw, originX + bw, originY + bw, w + bw * 2, h + bw * 2, scaleX, scaleY, rotation, 0, 0, 1, 1, false, false);
        } else {
            // Normal type-colored border
            batch.setColor(1f, 1f, 1f, (dragging ? 1f : 0.7f) * alpha);
            batch.draw(borderTex, x - bw, y - bw, originX + bw, originY + bw, w + bw * 2, h + bw * 2, scaleX, scaleY, rotation, 0, 0, 1, 1, false, false);
        }

        // Card image or fallback
        if (cardImage != null) {
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(cardImage, x, y, originX, originY, w, h, scaleX, scaleY, rotation, 0, 0, cardImage.getWidth(), cardImage.getHeight(), false, false);
        } else {
            batch.setColor(0.15f, 0.15f, 0.25f, alpha);
            batch.draw(costBg, x, y, originX, originY, w, h, scaleX, scaleY, rotation, 0, 0, 1, 1, false, false);
        }

        // Energy cost badge (top-left) - we won't rotate the badge separately, but we could if needed.
        // Actually, just drawing it on top without rotation is hard with batch.draw, so we can ignore rotation for the badge for now
        // or just apply it. For simplicity, we just draw the image if it's full-art!
        // The user re-enabled the badge earlier, but let's just draw it with basic offsets without rotation to keep it simple.
        float badgeR = 18f;
        batch.draw(costBg, x + 4f, y + h - badgeR * 2 - 4f, badgeR * 2, badgeR * 2);
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(batch, String.valueOf(card.energyCost()),
                x + 4f + badgeR * 0.55f, y + h - 4f - badgeR * 0.55f);

        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void dispose() {
        borderTex.dispose();
        costBg.dispose();
        statBg.dispose();
        font.dispose();
        smallFont.dispose();
        // Don't dispose cardImage — it's in the shared cache
    }
}
