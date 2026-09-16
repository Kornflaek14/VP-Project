package com.cardgame;
import com.cardgame.logic.cards.AbstractCard;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cardgame.data.*;
import com.cardgame.logic.relics.*;
import com.cardgame.logic.potions.*;
import com.cardgame.ui.DevModeOverlay;
import com.cardgame.utils.Constants;

import java.util.*;

/**
 * Root ApplicationListener.
 * Loads all game data from JSON and bootstraps the first screen.
 */
public class CardBattlerGame extends Game {

    private Stage devStage;
    private DevModeOverlay devOverlay;
    private TextButton devBadge;
    private Texture badgeNormalTex;
    private Texture badgeHoverTex;
    private BitmapFont badgeFont;

    private List<AbstractCard> allCards = Arrays.asList(
        new com.cardgame.logic.cards.FranticStrikeCard(),
        new com.cardgame.logic.cards.DenyRealityCard(),
        new com.cardgame.logic.cards.SkullCrackCard(),
        new com.cardgame.logic.cards.DelusionalStrikeCard(),
        new com.cardgame.logic.cards.DescentIntoMadnessCard(),
        new com.cardgame.logic.cards.HysteriaCard(),
        new com.cardgame.logic.cards.IntrusiveThoughtCard(),
        new com.cardgame.logic.cards.LashOutCard(),
        new com.cardgame.logic.cards.ManicBurstCard(),
        new com.cardgame.logic.cards.ManifestTraumaCard(),
        new com.cardgame.logic.cards.ObsessiveStrikeCard(),
        new com.cardgame.logic.cards.PsychoticBreakCard(),
        new com.cardgame.logic.cards.RecklessAbandonCard(),
        new com.cardgame.logic.cards.RepressCard(),
        new com.cardgame.logic.cards.SeethingHatredCard(),
        new com.cardgame.logic.cards.SharpenMindCard(),
        new com.cardgame.logic.cards.SplitPersonalityCard(),
        new com.cardgame.logic.cards.SteelNerveCard(),
        new com.cardgame.logic.cards.StubbornDenialCard(),
        new com.cardgame.logic.cards.SuddenPanicCard(),
        new com.cardgame.logic.cards.TraumatizeCard(),
        new com.cardgame.logic.cards.ViciousStrikeCard(),
        new com.cardgame.logic.cards.WildFlailingCard()
    );
    private List<CharacterData> allCharacters = Collections.emptyList();
    private List<MonsterData>   allMonsters   = Collections.emptyList();
    
    

    @Override
    public void create() {
        try {
            
            Gdx.app.log("Game", "Loaded " + allCards.size() + " cards.");
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load cards.json", e);
        }
        try {
            allCharacters = com.cardgame.data.CardDataLoader.loadCharacters(Gdx.files.internal("data/characters.json").read());
            Gdx.app.log("Game", "Loaded " + allCharacters.size() + " characters.");
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load characters.json", e);
        }
        try {
            allMonsters = com.cardgame.data.CardDataLoader.loadMonsters(Gdx.files.internal("data/monsters.json").read());
            Gdx.app.log("Game", "Loaded " + allMonsters.size() + " monsters.");
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load monsters.json", e);
        }
        try {
            
            
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load relics.json", e);
        }
        try {
            
            
        } catch (Exception e) {
            Gdx.app.error("Game", "Failed to load potions.json", e);
        }

        initDevMode();
        setScreen(new com.cardgame.screens.MainMenuScreen(this));
    }

    public List<AbstractCard>      getAllCards()      { return allCards;      }
    public List<CharacterData> getAllCharacters() { return allCharacters; }
    public List<MonsterData>   getAllMonsters()   { return allMonsters;   }
    public List<AbstractRelic> getAllRelics() {
        return java.util.Arrays.asList(new TaintedIVBag(), new RustedScalpel(), new RorschachInkblot());
    }
    public List<AbstractPotion> getAllPotions() {
        return java.util.Arrays.asList(new AdrenalineSyringe(), new VialOfAcid(), new SteroidAmpoule());
    }

    /** Get cards for a specific character. */
    public List<AbstractCard> getCardsForCharacter(String charName) {
        List<AbstractCard> filtered = new ArrayList<>();
        for (AbstractCard c : allCards) {
            if (c.character().equalsIgnoreCase(charName)) filtered.add(c);
        }
        return filtered;
    }

    /** Get monsters for a given level tier. */
    public List<MonsterData> getMonstersForLevel(int level) {
        List<MonsterData> filtered = new ArrayList<>();
        for (MonsterData m : allMonsters) {
            if (m.level() == level) filtered.add(m);
        }
        return filtered;
    }

    private void initDevMode() {
        try {
            devStage = new Stage(new FitViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT));

            badgeFont = new BitmapFont();
            badgeFont.getData().setScale(0.95f);

            Pixmap pmNorm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pmNorm.setColor(new Color(0.08f, 0.12f, 0.18f, 0.85f));
            pmNorm.fill();
            badgeNormalTex = new Texture(pmNorm);
            pmNorm.dispose();

            Pixmap pmHov = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pmHov.setColor(new Color(0.24f, 0.35f, 0.52f, 0.95f));
            pmHov.fill();
            badgeHoverTex = new Texture(pmHov);
            pmHov.dispose();

            TextButton.TextButtonStyle badgeStyle = new TextButton.TextButtonStyle();
            badgeStyle.font = badgeFont;
            badgeStyle.fontColor = new Color(0.96f, 0.84f, 0.38f, 1f);
            badgeStyle.overFontColor = Color.WHITE;
            badgeStyle.up = new TextureRegionDrawable(new TextureRegion(badgeNormalTex));
            badgeStyle.over = new TextureRegionDrawable(new TextureRegion(badgeHoverTex));

            devBadge = new TextButton("[DEV: F1]", badgeStyle);
            devBadge.setPosition(Constants.VIEWPORT_WIDTH - 125f, Constants.VIEWPORT_HEIGHT - 32f);
            devBadge.setSize(115f, 26f);
            devBadge.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    toggleDevMode();
                }
            });

            devOverlay = new DevModeOverlay(this, () -> updateGlobalInputProcessor());

            devStage.addActor(devBadge);
            devStage.addActor(devOverlay);
        } catch (Exception e) {
            Gdx.app.error("Game", "Could not initialize DevMode overlay", e);
        }
    }

    public void toggleDevMode() {
        if (devOverlay == null) return;
        if (devOverlay.isOpen()) {
            devOverlay.hide();
        } else {
            devOverlay.show();
        }
        updateGlobalInputProcessor();
    }

    public void updateGlobalInputProcessor() {
        if (devStage == null) return;
        InputProcessor current = Gdx.input.getInputProcessor();
        if (current instanceof InputMultiplexer mux) {
            if (!mux.getProcessors().contains(devStage, true)) {
                mux.addProcessor(0, devStage);
            }
        } else if (current != devStage) {
            InputMultiplexer mux = new InputMultiplexer();
            mux.addProcessor(devStage);
            if (current != null) {
                mux.addProcessor(current);
            }
            Gdx.input.setInputProcessor(mux);
        }
    }

    @Override
    public void setScreen(com.badlogic.gdx.Screen screen) {
        if (devOverlay != null && devOverlay.isOpen()) {
            devOverlay.hide();
        }
        super.setScreen(screen);
        updateGlobalInputProcessor();
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1) || Gdx.input.isKeyJustPressed(Input.Keys.GRAVE)) {
            toggleDevMode();
        }
        super.render();
        if (devStage != null) {
            if (!(Gdx.input.getInputProcessor() instanceof InputMultiplexer)) {
                updateGlobalInputProcessor();
            }
            float delta = Gdx.graphics.getDeltaTime();
            devStage.act(delta);
            devStage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (devStage != null) {
            devStage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (devStage != null) devStage.dispose();
        if (devOverlay != null) devOverlay.disposeResources();
        if (badgeNormalTex != null) badgeNormalTex.dispose();
        if (badgeHoverTex != null) badgeHoverTex.dispose();
        if (badgeFont != null) badgeFont.dispose();
    }
}
