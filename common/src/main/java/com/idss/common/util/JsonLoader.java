package com.idss.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * JSON loader/writer for algorithm I/O (master_context_file.md Section 2.3).
 * Uses Jackson with JSR-310 support auto-registered so {@code java.time}
 * fields serialize as ISO-8601 strings.
 *
 * <p>Paths that are not absolute are resolved against the JVM working
 * directory, so callers can pass portable paths like
 * {@code "data/input/input_exams.json"}.</p>
 */
public final class JsonLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private JsonLoader() {
        throw new AssertionError("JsonLoader is a utility; do not instantiate.");
    }

    /** The shared, configured {@link ObjectMapper}. */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /** Resolves a path to a {@link File}, anchoring relative paths to {@code user.dir}. */
    public static File resolve(String path) {
        File file = new File(path);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(System.getProperty("user.dir"), path);
    }

    /** Loads a single JSON object from {@code path} into {@code type}. */
    public static <T> T load(String path, Class<T> type) throws IOException {
        return MAPPER.readValue(resolve(path), type);
    }

    /** Loads a JSON array from {@code path} into a {@code List<T>}. */
    public static <T> List<T> loadList(String path, Class<T> elementType) throws IOException {
        CollectionType collectionType = MAPPER.getTypeFactory()
                .constructCollectionType(List.class, elementType);
        return MAPPER.readValue(resolve(path), collectionType);
    }

    /** Writes {@code value} to {@code path} as pretty-printed JSON. */
    public static void write(String path, Object value) throws IOException {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(resolve(path), value);
    }
}
