package com.cardgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.cardgame.data.CardData;
import com.cardgame.data.CardDataLoader;
import com.cardgame.logic.abilities.AbilityRegistry;
import com.cardgame.screens.MainMenuScreen;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * Root {@link com.badlogic.gdx.ApplicationListener}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Load card templates from {@code assets/cards.json} once.</li>
 *   <li>Bootstrap the {@link AbilityRegistry}.</li>
 *   <li>Hand off to {@link MainMenuScreen} as the first screen.</li>
 * </ul>
 */
public class CardBattlerGame extends Game {

    private Map<String, CardData> cardTemplates = Collections.emptyMap();

    @Override
    public void create() {
        // 1. Register abilities before any game logic runs
        AbilityRegistry.getInstance().registerDefaults();

        // 2. Load card templates from JSON
        try (InputStreamReader reader = new InputStreamReader(
                Gdx.files.internal("assets/cards.json").read(), StandardCharsets.UTF_8)) {
            cardTemplates = CardDataLoader.loadAsMap(reader);
            Gdx.app.log("CardBattlerGame", "Loaded " + cardTemplates.size() + " card templates.");
        } catch (Exception e) {
            Gdx.app.error("CardBattlerGame", "Failed to load cards.json — using empty template map.", e);
        }

        // 3. Show first screen
        setScreen(new MainMenuScreen(this));
    }

    /** Returns the immutable card template map (non-null, never mutated after create()). */
    public Map<String, CardData> getCardTemplates() {
        return cardTemplates;
    }
}
