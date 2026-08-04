package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.cardgame.logic.CardInstance;
import com.cardgame.utils.Constants;

/**
 * Scene2D {@link Actor} that renders one {@link CardInstance} visually.
 * <p>
 * Responsibilities: draw card background, name, attack, health, taunt border.
 * Supports drag interaction via {@link CardInteractionCallback}.
 * <p>
 * The same actor class is used for hand cards and board cards; callers size
 * it appropriately via {@link #setSize}.
 */
public class CardActor extends Actor {

    // ── State ──────────────────────────────────────────────────────────────────
    private final CardInstance card;
    private boolean selected = false;
    private boolean lifted   = false;
    private float homeX, homeY; // snap-back position

    // ── Textures (per-instance; disposed in dispose()) ─────────────────────────
    private Texture cardBg;
    private Texture selectedBorder;
    private Texture tauntBorder;
    private Texture exhaustedOverlay;
    private Texture atkBadge;
    private Texture hpBadge;

    // ── Font ───────────────────────────────────────────────────────────────────
    private final BitmapFont font;

    // ── Colors ─────────────────────────────────────────────────────────────────
    private static final Color BG_NORMAL     = new Color(0.18f, 0.22f, 0.32f, 1f);
    private static final Color BG_TAUNT      = new Color(0.22f, 0.14f, 0.06f, 1f);
    private static final Color BG_BLADE      = new Color(0.3f, 0.3f, 0.4f, 1f);
    private static final Color BG_CHEMICAL   = new Color(0.2f, 0.4f, 0.2f, 1f);
    private static final Color BG_MEDICAL    = new Color(0.4f, 0.2f, 0.2f, 1f);
    private static final Color BG_BLUNT      = new Color(0.4f, 0.3f, 0.1f, 1f);
    private static final Color BG_ELECTRIC   = new Color(0.2f, 0.3f, 0.5f, 1f);
    
    private static final Color BORDER_SELECT = new Color(0.96f, 0.84f, 0.38f, 1f);
    private static final Color BORDER_TAUNT  = new Color(0.85f, 0.45f, 0.12f, 1f);
    private static final Color COLOR_ATK     = new Color(0.95f, 0.35f, 0.25f, 1f);
    private static final Color COLOR_HP      = new Color(0.25f, 0.80f, 0.35f, 1f);
    private static final Color EXHAUSTED_TINT = new Color(0f, 0f, 0f, 0.42f);

    // ── Callback ───────────────────────────────────────────────────────────────
    
    /** Callback for card interactions (click for board cards, drag for hand cards). */
    public interface CardInteractionCallback {
        /** Called on simple click (board cards). */
        default void onClick(CardActor actor) {}
        /** Called when drag starts (hand cards). */
        default void onDragStart(CardActor actor) {}
        /** Called each frame during drag. */
        default void onDrag(CardActor actor, float stageX, float stageY) {}
        /** Called when drag ends (mouse up). */
        default void onDragEnd(CardActor actor, float stageX, float stageY) {}
    }

    /** Legacy click-only callback for board cards. */
    public interface OnClickCallback { void onClick(CardActor actor); }

    /**
     * Creates a CardActor with drag support (for hand cards).
     */
    public CardActor(CardInstance card, CardInteractionCallback callback) {
        this.card = card;
        this.font = new BitmapFont();
        this.font.getData().setScale(0.85f);
        this.font.setColor(Color.WHITE);

        buildTextures();

        addListener(new InputListener() {
            private boolean dragging = false;
            
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true; // claim the event
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (!dragging) {
                    dragging = true;
                    if (callback != null) callback.onDragStart(CardActor.this);
                }
                if (callback != null) {
                    // Convert to stage coordinates
                    com.badlogic.gdx.math.Vector2 stagePos = CardActor.this.localToStageCoordinates(
                            new com.badlogic.gdx.math.Vector2(x, y));
                    callback.onDrag(CardActor.this, stagePos.x, stagePos.y);
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (dragging) {
                    dragging = false;
                    if (callback != null) {
                        com.badlogic.gdx.math.Vector2 stagePos = CardActor.this.localToStageCoordinates(
                                new com.badlogic.gdx.math.Vector2(x, y));
                        callback.onDragEnd(CardActor.this, stagePos.x, stagePos.y);
                    }
                } else {
                    // Simple click (no drag occurred)
                    if (callback != null) callback.onClick(CardActor.this);
                }
            }
        });
    }

    /**
     * Creates a CardActor with simple click support (for board cards).
     */
    public CardActor(CardInstance card, OnClickCallback callback) {
        this.card = card;
        this.font = new BitmapFont();
        this.font.getData().setScale(0.85f);
        this.font.setColor(Color.WHITE);

        buildTextures();

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (callback != null) callback.onClick(CardActor.this);
            }
        });
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void buildTextures() {
        Color bgColor = BG_NORMAL;
        if (card.hasTaunt()) {
            bgColor = BG_TAUNT;
        } else if (card.getTemplate().affinityType() != null) {
            switch (card.getTemplate().affinityType()) {
                case BLADE: bgColor = BG_BLADE; break;
                case CHEMICAL: bgColor = BG_CHEMICAL; break;
                case MEDICAL: bgColor = BG_MEDICAL; break;
                case BLUNT: bgColor = BG_BLUNT; break;
                case ELECTRIC: bgColor = BG_ELECTRIC; break;
                default: break;
            }
        }
        
        cardBg          = singlePixel(bgColor);
        selectedBorder  = singlePixel(BORDER_SELECT);
        tauntBorder     = singlePixel(BORDER_TAUNT);
        exhaustedOverlay = singlePixel(EXHAUSTED_TINT);
        atkBadge        = singlePixel(COLOR_ATK);
        hpBadge         = singlePixel(COLOR_HP);
    }

    private static Texture singlePixel(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public CardInstance getCard()           { return card;     }
    public boolean      isSelected()        { return selected; }
    public void         setSelected(boolean s) { selected = s; }
    
    public boolean      isLifted()          { return lifted;   }
    public void         setLifted(boolean l) { lifted = l;     }
    
    /** Store current position as the snap-back home position. */
    public void saveHomePosition() {
        homeX = getX();
        homeY = getY();
    }
    
    public float getHomeX() { return homeX; }
    public float getHomeY() { return homeY; }
    
    private float currentLift = 0f;

    @Override
    public void act(float delta) {
        super.act(delta);
        float targetLift = lifted ? Constants.CARD_HEIGHT * 0.2f : 0f;
        currentLift = com.badlogic.gdx.math.MathUtils.lerp(currentLift, targetLift, 15f * delta);
        
        float targetScale = lifted ? 1.15f : 1.0f;
        float currentScale = com.badlogic.gdx.math.MathUtils.lerp(getScaleX(), targetScale, 15f * delta);
        setScale(currentScale);
    }

    /** Snap card back to its home position smoothly. */
    public void snapBack() {
        setLifted(false);
        addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(homeX, homeY, 0.25f, com.badlogic.gdx.math.Interpolation.pow2Out));
    }

    // ── Scene2D draw ───────────────────────────────────────────────────────────

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float baseW = getWidth();
        float baseH = getHeight();
        float scale = getScaleX() == 0 ? 1f : getScaleX(); // safe fallback
        float w = baseW * scale;
        float h = baseH * scale;
        float x = getX() + (baseW - w) / 2f;
        float y = getY() + currentLift + (baseH - h) / 2f;
        
        float alpha = parentAlpha * getColor().a;

        // ── Border ──────────────────────────────────────────────────────────
        float border = 3f;
        Texture borderTex = selected    ? selectedBorder
                           : card.hasTaunt() ? tauntBorder
                           : null;
        if (borderTex != null) {
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(borderTex, x - border, y - border, w + border * 2, h + border * 2);
        }

        // ── Card background ──────────────────────────────────────────────────
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(cardBg, x, y, w, h);

        // ── Exhausted overlay ────────────────────────────────────────────────
        if (card.isExhausted()) {
            batch.setColor(1f, 1f, 1f, 0.5f * alpha);
            batch.draw(exhaustedOverlay, x, y, w, h);
            batch.setColor(1f, 1f, 1f, alpha);
        }

        // ── Cost badges (top-left) ───────────────────────────────────────────
        int blood = card.getTemplate().bloodCost();
        int bone = card.getTemplate().boneCost();
        String costStr = "";
        if (blood > 0) costStr += blood + " Blood ";
        if (bone > 0) costStr += bone + " Bone";
        font.setColor(new Color(0.85f, 0.25f, 0.25f, alpha));
        font.draw(batch, costStr.trim(), x + 6f, y + h - 6f);

        // ── Card name (centred, top third) ───────────────────────────────────
        font.setColor(new Color(1f, 1f, 1f, alpha));
        String name = card.getTemplate().name();
        // Truncate long names
        if (name.length() > 10) name = name.substring(0, 9) + "…";
        font.draw(batch, name, x + 5f, y + h - 22f);

        // ── Description (small, middle) ──────────────────────────────────────
        if (!card.getTemplate().abilityIds().isEmpty()) {
            font.getData().setScale(0.65f);
            font.setColor(new Color(0.9f, 0.75f, 0.4f, alpha));
            String tag = String.join(", ", card.getTemplate().abilityIds()).toUpperCase();
            font.draw(batch, tag, x + 5f, y + h * 0.45f);
            font.getData().setScale(0.85f);
        }

        // ── ATK badge (bottom-left) ──────────────────────────────────────────
        float badgeSize = 24f;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(atkBadge, x + 4f, y + 4f, badgeSize, badgeSize);
        font.setColor(new Color(1f, 1f, 1f, alpha));
        font.draw(batch, String.valueOf(card.getTemplate().attack()),
                x + 4f + badgeSize * 0.25f, y + 4f + badgeSize * 0.72f);

        // ── HP badge (bottom-right) ──────────────────────────────────────────
        Color hpColor = card.getCurrentHealth() < card.getTemplate().health()
                        ? new Color(0.95f, 0.3f, 0.3f, alpha)   // damaged → red
                        : new Color(0.25f, 0.80f, 0.35f, alpha); // full → green
        batch.setColor(hpColor);
        batch.draw(hpBadge, x + w - badgeSize - 4f, y + 4f, badgeSize, badgeSize);
        batch.setColor(1f, 1f, 1f, alpha);
        font.setColor(new Color(1f, 1f, 1f, alpha));
        font.draw(batch, String.valueOf(card.getCurrentHealth()),
                x + w - badgeSize + 2f, y + 4f + badgeSize * 0.72f);

        // Reset batch color
        batch.setColor(1f, 1f, 1f, 1f);
    }

    // ── Disposal ───────────────────────────────────────────────────────────────

    public void dispose() {
        cardBg.dispose();
        selectedBorder.dispose();
        tauntBorder.dispose();
        exhaustedOverlay.dispose();
        atkBadge.dispose();
        hpBadge.dispose();
        font.dispose();
    }
}
