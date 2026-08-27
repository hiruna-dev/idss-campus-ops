package com.idss.task2.config;

import com.idss.common.config.MongoConnection;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

/**
 * MongoDB wiring for Task 2 (master_context_file.md Section 2.3 / Section 5).
 *
 * <p>Spring Boot's {@code spring.data.mongodb.uri} property reads from system
 * environment variables, not from the project's gitignored {@code .env} file.
 * To keep a single source of truth, this config reuses {@link MongoConnection}
 * (which loads {@code .env} via dotenv-java, walking up from the module cwd to
 * the project root) and exposes the beans Spring Data MongoDB expects. With a
 * {@link MongoDatabaseFactory} bean present, Spring Boot's Mongo
 * auto-configuration backs off and uses this one.</p>
 */
@Configuration
public class MongoConfig {

    @Bean
    public MongoClient mongoClient() {
        Dotenv env = MongoConnection.loadEnv();
        String uri = env.get("MONGO_URI");
        if (uri == null || uri.isBlank()) {
            uri = MongoConnection.DEFAULT_URI;
        }
        return MongoClients.create(uri);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        Dotenv env = MongoConnection.loadEnv();
        String dbName = env.get("MONGO_DATABASE");
        if (dbName == null || dbName.isBlank()) {
            dbName = MongoConnection.DEFAULT_DATABASE;
        }
        return new SimpleMongoClientDatabaseFactory(mongoClient, dbName);
    }
}
