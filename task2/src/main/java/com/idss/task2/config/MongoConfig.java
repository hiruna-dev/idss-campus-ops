package com.idss.task2.config;

import com.idss.common.config.MongoConnection;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

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
