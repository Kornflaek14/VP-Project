package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cardgame.logic.cards.AbstractCard;
import com.cardgame.utils.Constants;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * A tooltip overlay that shows card keywords and descriptions
 * next to the currently hovered card in the player's hand.
 */
public class CardPreviewOverlay extends Group {

    private CardActor hoveredActor = null;

    private final BitmapFont titleFont;
    private final BitmapFont descFont;
    private final Texture bgTex;
    private final Texture borderTex;

    private static final float BOX_WIDTH = 280f;
    private static final float PADDING = 15f;

    // Hardcoded keyword dictionary
    private static final Map<String, String> KEYWORDS = new LinkedHashMap<>();
    static {
        KEYWORDS.put("Block", "Until next turn, prevents damage.");
        KEYWORDS.put("Vulnerable", "Target takes 50% more damage from attacks.");
        KEYWORDS.put("Weak", "Target deals 25% less damage with attacks.");
        KEYWORDS.put("Exhaust", "Card is removed from combat after being played.");
        KEYWORDS.put("Strength", "Increases damage dealt by attacks.");
        KEYWORDS.put("Dexterity", "Increases Block gained from cards.");
        KEYWORDS.put("Poison", "Target takes damage at the start of its turn. Loses 1 stack per turn.");
    }

    public CardPreviewOverlay() {
        titleFont = new BitmapFont();
        titleFont.getData().setScale(1.1f);
        titleFont.setColor(new Color(0.95f, 0.85f, 0.4f, 1f)); // Slay the spire yellow title

        descFont = new BitmapFont();
        descFont.getData().setScale(1.0f);
        descFont.setColor(Color.WHITE);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.12f, 0.15f, 0.18f, 0.95f)); // Dark grey/blue bg
        pm.fill();
        bgTex = new Texture(pm);

        pm.setColor(new Color(0.4f, 0.45f, 0.5f, 1f)); // Grey border
        pm.fill();
        borderTex = new Texture(pm);
        pm.dispose();

        setVisible(false);
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    public void show(CardActor actor) {
        this.hoveredActor = actor;
        setVisible(true);
    }

    public void hide() {
        this.hoveredActor = null;
        setVisible(false);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!isVisible() || hoveredActor == null || hoveredActor.isDragging()) return;

        AbstractCard card = hoveredActor.getCard();
        String desc = card.description();
        
        // Find all keywords in the description
        Map<String, String> foundKeywords = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : KEYWORDS.entrySet()) {
            // Check if word exists in description (case-insensitive whole word match)
            if (Pattern.compile("(?i)\\b" + entry.getKey() + "\\b").matcher(desc).find()) {
                foundKeywords.put(entry.getKey(), entry.getValue());
            }
        }
        
        // If no keywords found, don't draw any tooltips!
        if (foundKeywords.isEmpty()) return;

        float scale = hoveredActor.getScaleX();
        float cardWidth = hoveredActor.getWidth() * scale;
        float cardX = hoveredActor.getX() - (cardWidth - hoveredActor.getWidth()) / 2f;
        float cardY = hoveredActor.getY();

        // Calculate total height of all tooltips to adjust starting Y position
        float totalHeight = 0;
        for (String keyword : foundKeywords.keySet()) {
            totalHeight += 95f; // rough height per box
        }

        float x = cardX + cardWidth + 20f;
        float startY = cardY + (hoveredActor.getHeight() * scale) / 2f - totalHeight / 2f;

        // If it goes off the right edge, move it to the left side
        if (x + BOX_WIDTH > Constants.VIEWPORT_WIDTH) {
            x = cardX - BOX_WIDTH - 20f;
        }
        
        // Clamp to screen top/bottom margins
        if (startY < 20f) startY = 20f;
        if (startY + totalHeight > Constants.VIEWPORT_HEIGHT - 20f) startY = Constants.VIEWPORT_HEIGHT - totalHeight - 20f;

        batch.setColor(1, 1, 1, parentAlpha);
        
        float currentY = startY + totalHeight;

        // Draw a box for each keyword
        for (Map.Entry<String, String> entry : foundKeywords.entrySet()) {
            float boxHeight = 90f; // Fixed height for simplicity
            currentY -= boxHeight;
            
            // Draw Slay the Spire style thick border
            float bw = 4f;
            batch.draw(borderTex, x - bw, currentY - bw, BOX_WIDTH + bw * 2, boxHeight + bw * 2);

            // Draw background
            batch.draw(bgTex, x, currentY, BOX_WIDTH, boxHeight);

            // Draw Title (Keyword)
            titleFont.draw(batch, entry.getKey(), x + PADDING, currentY + boxHeight - PADDING);

            // Draw Description (Definition)
            descFont.draw(batch, entry.getValue(), x + PADDING, currentY + boxHeight - PADDING - 25f, BOX_WIDTH - PADDING * 2, com.badlogic.gdx.utils.Align.left, true);
            
            currentY -= 5f; // gap between boxes
        }
        
        batch.setColor(1, 1, 1, 1);
    }

    public void disposeResources() {
        titleFont.dispose();
        descFont.dispose();
        bgTex.dispose();
        borderTex.dispose();
    }
}
