package com.example.urlshortener.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

class DatabaseUrlEnvironmentInitializerTest {

    @Test
    void convertsPostgresUrlToJdbcUrlAndExtractsCredentials() {
        GenericApplicationContext context = new GenericApplicationContext();
        ConfigurableEnvironment env = context.getEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("testEnv", Map.of(
            "DB_URL", "postgres://testuser:testpass@dpg-abc123-a:5432/urlshortener_db"
        )));

        new DatabaseUrlEnvironmentInitializer().initialize(context);

        assertEquals("jdbc:postgresql://dpg-abc123-a:5432/urlshortener_db", env.getProperty("spring.datasource.url"));
        assertEquals("testuser", env.getProperty("spring.datasource.username"));
        assertEquals("testpass", env.getProperty("spring.datasource.password"));
    }

    @Test
    void convertsDatabaseUrlWithPostgresqlSchemeAndQueryParams() {
        GenericApplicationContext context = new GenericApplicationContext();
        ConfigurableEnvironment env = context.getEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("testEnv", Map.of(
            "DATABASE_URL", "postgresql://admin:secret@ep-cool-db.us-east-1.render.com:5432/shortdb?sslmode=require"
        )));

        new DatabaseUrlEnvironmentInitializer().initialize(context);

        assertEquals("jdbc:postgresql://ep-cool-db.us-east-1.render.com:5432/shortdb?sslmode=require", env.getProperty("spring.datasource.url"));
        assertEquals("admin", env.getProperty("spring.datasource.username"));
        assertEquals("secret", env.getProperty("spring.datasource.password"));
    }

    @Test
    void mapsRedisUrlWhenPresent() {
        GenericApplicationContext context = new GenericApplicationContext();
        ConfigurableEnvironment env = context.getEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("testEnv", Map.of(
            "REDIS_URL", "rediss://default:mypass@red-abc:6379"
        )));

        new DatabaseUrlEnvironmentInitializer().initialize(context);

        assertEquals("rediss://default:mypass@red-abc:6379", env.getProperty("spring.data.redis.url"));
    }

    @Test
    void leavesStandardJdbcUrlUntouched() {
        GenericApplicationContext context = new GenericApplicationContext();
        ConfigurableEnvironment env = context.getEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("testEnv", Map.of(
            "DB_URL", "jdbc:postgresql://localhost:5432/urlshortener"
        )));

        new DatabaseUrlEnvironmentInitializer().initialize(context);

        assertNull(env.getProperty("spring.datasource.username"));
    }
}
