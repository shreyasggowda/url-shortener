package com.example.urlshortener.config;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class DatabaseUrlEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        Map<String, Object> overrides = new HashMap<>();

        // 1. PostgreSQL URL handling (Render provides DATABASE_URL or user sets DB_URL as postgres:// or postgresql://)
        String dbUrl = env.getProperty("DB_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = env.getProperty("DATABASE_URL");
        }

        if (dbUrl != null && !dbUrl.isBlank()) {
            if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
                try {
                    String cleanUriStr = dbUrl.startsWith("postgres://")
                            ? "postgresql://" + dbUrl.substring("postgres://".length())
                            : dbUrl;
                    URI uri = URI.create(cleanUriStr);

                    String host = uri.getHost();
                    int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                    String path = uri.getPath();
                    String query = uri.getQuery();

                    String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                    if (query != null && !query.isBlank()) {
                        jdbcUrl += "?" + query;
                    }

                    overrides.put("spring.datasource.url", jdbcUrl);

                    if (uri.getUserInfo() != null) {
                        String[] userInfo = uri.getUserInfo().split(":", 2);
                        overrides.put("spring.datasource.username", userInfo[0]);
                        if (userInfo.length > 1) {
                            overrides.put("spring.datasource.password", userInfo[1]);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // 2. Redis URL handling (Render automatically sets REDIS_URL)
        String redisUrl = env.getProperty("REDIS_URL");
        if (redisUrl != null && !redisUrl.isBlank()) {
            overrides.put("spring.data.redis.url", redisUrl);
        }

        if (!overrides.isEmpty()) {
            env.getPropertySources().addFirst(new MapPropertySource("cloudDeploymentOverrides", overrides));
        }
    }
}
