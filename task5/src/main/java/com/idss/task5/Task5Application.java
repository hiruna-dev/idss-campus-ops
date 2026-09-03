package com.idss.task5;

import com.idss.common.config.MongoConnection;
import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Task5Application {

    public static void main(String[] args) {
        loadEnvIntoSystemProperties();
        SpringApplication.run(Task5Application.class, args);
    }

    /**
     * Loads the gitignored {@code .env} (if present) into System properties
     * so application.yml's {@code ${MONGO_URI:...}} / {@code ${MONGO_DATABASE:...}}
     * placeholders can resolve real credentials without committing them to a
     * tracked file. An explicit OS environment variable always wins over
     * {@code .env} — this only fills in what isn't already set.
     *
     * <p>Without this, Task5 was the only service with no {@code .env} bridging
     * at all, so it silently fell back to the yaml default (local mongod)
     * instead of the shared Atlas cluster every other service resolves to.</p>
     */
    private static void loadEnvIntoSystemProperties() {
        Dotenv dotenv = MongoConnection.loadEnv();
        setIfAbsent("MONGO_URI", dotenv.get("MONGO_URI"));
        setIfAbsent("MONGO_DATABASE", dotenv.get("MONGO_DATABASE"));
    }

    private static void setIfAbsent(String key, String value) {
        if (value != null && !value.isBlank()
                && System.getProperty(key) == null
                && System.getenv(key) == null) {
            System.setProperty(key, value);
        }
    }
}
