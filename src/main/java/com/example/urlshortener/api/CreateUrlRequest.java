package com.example.urlshortener.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;

public record CreateUrlRequest(
    @NotBlank @Pattern(regexp = "https?://.+", message = "Enter a valid HTTP or HTTPS URL, including https://") String longUrl,
    String customAlias,
    Instant expiresAt
) { }
