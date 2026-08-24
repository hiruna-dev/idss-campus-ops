package com.idss.common.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;

/**
 * MongoDB connection utility (master_context_file.md Section 2.3 / Section 5).
 * Reads {@code MONGO_URI} and {@code MONGO_DATABASE} from {@code .env} via
 * dotenv-java, falling back to sensible localhost defaults when the file or
 * keys are missing.
 *
 * <p>Searches for {@code .env} in the current directory and up to two parent
 * directories, so it works whether the JVM is launched from the project root
 * or from a submodule directory (e.g. {@code mvn -pl common exec:java} runs
 * with cwd = {@code common/}, but {@code .env} lives in the project root).</p>
 */
public final class MongoConnection {

    public static final String DEFAULT_URI = "mongodb://localhost:27017";
    public static final String DEFAULT_DATABASE = "idss";

    private MongoConnection() {
        throw new AssertionError("MongoConnection is a utility; do not instantiate.");
    }

    /**
     * Loads {@code .env} from the first directory (starting at cwd, then up to
     * two parents) that contains one. Returns an empty dotenv view if none is
     * found.
     */
    public static Dotenv loadEnv() {
        File cwd = new File(System.getProperty("user.dir"));
        for (int i = 0; i < 3; i++) {
            File envFile = new File(cwd, ".env");
            if (envFile.exists()) {
                return Dotenv.configure()
                        .directory(cwd.getAbsolutePath())
                        .ignoreIfMissing()
                        .load();
            }
            cwd = cwd.getParentFile();
            if (cwd == null) break;
        }
        return Dotenv.configure().ignoreIfMissing().load();
    }

    /** Opens a client and returns the configured database. */
    public static MongoDatabase getDatabase() {
        return getDatabase(loadEnv());
    }

    /** Opens a client using the given dotenv view and returns the configured database. */
    public static MongoDatabase getDatabase(Dotenv env) {
        String uri = env.get("MONGO_URI");
        if (uri == null || uri.isBlank()) {
            uri = DEFAULT_URI;
        }
        String dbName = env.get("MONGO_DATABASE");
        if (dbName == null || dbName.isBlank()) {
            dbName = DEFAULT_DATABASE;
        }
        MongoClient client = MongoClients.create(uri);
        return client.getDatabase(dbName);
    }
}
