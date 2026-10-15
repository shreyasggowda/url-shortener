package com.example.urlshortener;

import com.example.urlshortener.config.DatabaseUrlEnvironmentInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class UrlShortenerApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(UrlShortenerApplication.class);
        app.addInitializers(new DatabaseUrlEnvironmentInitializer());
        app.run(args);
    }
}

