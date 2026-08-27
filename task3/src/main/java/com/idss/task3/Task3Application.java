package com.idss.task3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Task3Application {
    public static void main(String[] args) {
        io.github.cdimascio.dotenv.Dotenv dotenv = com.idss.common.config.MongoConnection.loadEnv();
        if (dotenv.get("MONGO_URI") != null) {
            System.setProperty("MONGO_URI", dotenv.get("MONGO_URI"));
        }
        if (dotenv.get("MONGO_DATABASE") != null) {
            System.setProperty("MONGO_DATABASE", dotenv.get("MONGO_DATABASE"));
        }
        SpringApplication.run(Task3Application.class, args);
    }
}
