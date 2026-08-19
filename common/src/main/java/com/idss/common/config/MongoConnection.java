package com.idss.common.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * MongoDB connection utility (master_context_file.md Section 2.3 / Section 5).
 * Reads {@code MONGO_URI} and {@code MONGO_DATABASE} from {@code .env} via
 * dotenv-java, falling back to sensible localhost defaults when the file or
 * keys are missing.
 */
public final class MongoConnection {

    public static final String DEFAULT_URI = "mongodb://localhost:27017";
    public static final String DEFAULT_DATABASE = "idss_campus_ops";

    private MongoConnection() {
        throw new AssertionError("MongoConnection is a utility; do not instantiate.");
    }

    /** Loads {@code .env} if present, otherwise returns an empty dotenv view. */
    public static Dotenv loadEnv() {
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
