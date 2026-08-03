package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cardgame.logic.CardInstance;
import com.cardgame.utils.Constants;

/**
 * Scene2D {@link Actor} that renders one {@link CardInstance} visually.
 * <p>
 * Responsibilities: draw card background, name, attack, health, taunt border.
 * No game rules live here — input events call back to {@link BattleInputHandler}.
 * <p>
 * The same actor class is used for hand cards and board cards; callers size
 * it appropriately via {@link #setSize}.
 */
public class CardActor extends Actor {

    // ── State ──────────────────────────────────────────────────────────────────
    private final CardInstance card;
    private boolean selected = false;

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
    public interface OnClickCallback { void onClick(CardActor actor); }

    public CardActor(CardInstance card, OnClickCallback callback) {
        this.card = card;
        this.font = new BitmapFont();
        this.font.getData().setScale(0.85f);
        this.font.setColor(Color.WHITE);

        buildTextures();

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
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

    // ── Scene2D draw ───────────────────────────────────────────────────────────

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), w = getWidth(), h = getHeight();
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
