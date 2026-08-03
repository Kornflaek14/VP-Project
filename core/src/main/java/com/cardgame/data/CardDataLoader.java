package com.cardgame.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parses a JSON array of card definitions into {@link CardData} objects.
 *
 * <pre>
 * // Game runtime (inside a libGDX lifecycle method):
 * FileHandle fh = Gdx.files.internal("assets/cards.json");
 * Map&lt;String, CardData&gt; templates =
 *     CardDataLoader.loadAsMap(new InputStreamReader(fh.read(), StandardCharsets.UTF_8));
 *
 * // Unit test (no libGDX context required):
 * InputStream is = CardDataLoaderTest.class.getResourceAsStream("/cards.json");
 * List&lt;CardData&gt; cards = CardDataLoader.load(is);
 * </pre>
 *
 * HARD RULE: this class must NOT import any libGDX class.
 */
public final class CardDataLoader {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    private static final Type LIST_TYPE = new TypeToken<List<CardData>>() {}.getType();

    private CardDataLoader() {}

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Parses an ordered list of {@link CardData} from the supplied {@link Reader}.
     * Caller is responsible for closing the reader.
     */
    public static List<CardData> load(Reader reader) {
        List<CardData> cards = GSON.fromJson(reader, LIST_TYPE);
        return cards == null ? Collections.emptyList() : Collections.unmodifiableList(cards);
    }

    /**
     * Convenience overload: reads from an {@link InputStream} using UTF-8.
     */
    public static List<CardData> load(InputStream stream) {
        return load(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    /**
     * Loads cards and returns them indexed by {@link CardData#id()}.
     */
    public static Map<String, CardData> loadAsMap(Reader reader) {
        return load(reader).stream()
                .collect(Collectors.toUnmodifiableMap(CardData::id, c -> c));
    }

    /**
     * Convenience overload: reads from an {@link InputStream} using UTF-8.
     */
    public static Map<String, CardData> loadAsMap(InputStream stream) {
        return loadAsMap(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
