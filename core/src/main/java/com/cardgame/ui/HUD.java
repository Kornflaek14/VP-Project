package com.cardgame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.cardgame.logic.GameState;
import com.cardgame.utils.Constants;

public class HUD extends Group {

    private final BitmapFont font;
    private final BitmapFont largeFont;
    private final Texture barTexture;

    private GameState snapshot;
    private final TextButton endTurnBtn;

    public HUD(GameState initialState, ChangeListener endTurnListener) {
        this.snapshot = initialState;

        font = new BitmapFont();
        font.getData().setScale(1.2f);
        
        largeFont = new BitmapFont();
        largeFont.getData().setScale(2.0f);
        largeFont.setColor(Color.WHITE);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0.1f, 0.1f, 0.15f, 0.8f));
        pm.fill();
        barTexture = new Texture(pm);
        pm.dispose();

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = Color.YELLOW;

        endTurnBtn = new TextButton("END TURN", btnStyle);
        endTurnBtn.setSize(180, 60);
        endTurnBtn.setPosition(Constants.VIEWPORT_WIDTH - 200, 20);
        endTurnBtn.addListener(endTurnListener);
        addActor(endTurnBtn);
    }

    public void update(GameState state) {
        this.snapshot = state;
        endTurnBtn.setVisible(state.isPlayerTurn());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (snapshot == null) { super.draw(batch, parentAlpha); return; }

        float w = Constants.VIEWPORT_WIDTH;
        
        // Bottom bar background
        batch.setColor(1, 1, 1, parentAlpha);
        batch.draw(barTexture, 0, 0, w, 40);
        
        // Energy Display (Bottom Left)
        largeFont.setColor(Color.CYAN);
        largeFont.draw(batch, snapshot.playerEnergy + "/" + snapshot.playerMaxEnergy, 40, 60);
        font.setColor(Color.WHITE);
        font.draw(batch, "ENERGY", 40, 25);

        // Player HP & Block (Bottom Center)
        String hpStr = "HP: " + snapshot.playerHp + "/" + snapshot.playerMaxHp;
        if (snapshot.playerBlock > 0) {
            hpStr += "   [BLOCK: " + snapshot.playerBlock + "]";
        }
        font.setColor(Color.GREEN);
        font.draw(batch, hpStr, w / 2f - 100, 25);

        // Draw / Discard pile counts (Bottom Right area)
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Draw: " + snapshot.drawPile.size() + "   Discard: " + snapshot.discardPile.size(), w - 450, 25);

        // Top HUD for Monster (simplified)
        font.setColor(Color.RED);
        font.draw(batch, snapshot.monsterName + " HP: " + snapshot.monsterHp + "/" + snapshot.monsterMaxHp, w / 2f - 100, Constants.VIEWPORT_HEIGHT - 20);
        
        if (snapshot.monsterBlock > 0) {
            font.setColor(Color.CYAN);
            font.draw(batch, "[BLOCK: " + snapshot.monsterBlock + "]", w / 2f + 100, Constants.VIEWPORT_HEIGHT - 20);
        }

        // Monster Intent
        if (snapshot.isPlayerTurn()) {
            String intentStr = snapshot.intentType.equals("ATTACK") 
                ? "⚔ Will Attack for " + snapshot.intentValue 
                : "🛡 Will Defend for " + snapshot.intentValue;
            font.setColor(Color.YELLOW);
            font.draw(batch, intentStr, w / 2f - 80, Constants.VIEWPORT_HEIGHT - 60);
        }

        super.draw(batch, parentAlpha);
    }

    public void disposeResources() {
        font.dispose();
        largeFont.dispose();
        barTexture.dispose();
    }
}
