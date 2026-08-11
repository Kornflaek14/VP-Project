package com.cardgame.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Loads all game data from JSON files under assets/data/.
 */
public final class CardDataLoader {

    private static final Gson GSON = new Gson();

    private CardDataLoader() {}

    // ── Cards ──────────────────────────────────────────────────

    /** Loads cards from a Reader (for backward-compat). */
    public static List<CardData> loadCards(Reader reader) {
        Type listType = new TypeToken<List<CardData>>() {}.getType();
        List<CardData> list = GSON.fromJson(reader, listType);
        return list == null ? Collections.emptyList() : list;
    }

    /** Loads cards from an InputStream. */
    public static List<CardData> loadCards(InputStream is) {
        return loadCards(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    /** Loads cards as a map keyed by id. */
    public static Map<String, CardData> loadAsMap(Reader reader) {
        List<CardData> list = loadCards(reader);
        Map<String, CardData> map = new LinkedHashMap<>();
        for (CardData c : list) map.put(c.id(), c);
        return map;
    }

    public static Map<String, CardData> loadAsMap(InputStream is) {
        return loadAsMap(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    // ── Characters ─────────────────────────────────────────────

    public static List<CharacterData> loadCharacters(InputStream is) {
        Type listType = new TypeToken<List<CharacterData>>() {}.getType();
        List<CharacterData> list = GSON.fromJson(
                new InputStreamReader(is, StandardCharsets.UTF_8), listType);
        return list == null ? Collections.emptyList() : list;
    }

    // ── Monsters ───────────────────────────────────────────────

    public static List<MonsterData> loadMonsters(InputStream is) {
        Type listType = new TypeToken<List<MonsterData>>() {}.getType();
        List<MonsterData> list = GSON.fromJson(
                new InputStreamReader(is, StandardCharsets.UTF_8), listType);
        return list == null ? Collections.emptyList() : list;
    }


}
