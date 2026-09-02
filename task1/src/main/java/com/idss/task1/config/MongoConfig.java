package com.idss.task1.config;

import com.idss.common.config.MongoConnection;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

/**
 * MongoDB connection configuration for Task 1 (Student B Deliverable - Subtask 4.4).
 *
 * <p>Spring Boot's own {@code spring.data.mongodb.uri} placeholder in
 * {@code application.yml} only resolves from real OS environment variables, not from
 * the project's {@code .env} file. This configuration bridges that gap by reading
 * {@code MONGO_URI} / {@code MONGO_DATABASE} through {@link MongoConnection#loadEnv()} —
 * the same dotenv-java convention every other module in this project uses (see
 * {@code common.util.DatabaseSeeder}) — and exposes the resulting
 * {@link MongoDatabaseFactory} bean, which Spring Boot then auto-wires into
 * {@code MongoTemplate} and {@code DeliveryRouteRepository}.</p>
 *
 * <p>The MongoDB driver connects lazily: creating this bean never blocks or fails
 * application startup if the cluster is unreachable (e.g. a standalone test run with
 * no network access). Connection errors only surface when a repository performs an
 * actual read/write.</p>
 */
@Configuration
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory() {
        Dotenv env = MongoConnection.loadEnv();

        String uri = env.get("MONGO_URI");
        if (uri == null || uri.isBlank()) {
            uri = MongoConnection.DEFAULT_URI;
        }
        String database = env.get("MONGO_DATABASE");
        if (database == null || database.isBlank()) {
            database = MongoConnection.DEFAULT_DATABASE;
        }

        log.info("Configuring Task 1 MongoDB connection for database '{}'", database);

        MongoClient client = MongoClients.create(uri);
        return new SimpleMongoClientDatabaseFactory(client, database);
    }
}
