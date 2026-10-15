package com.example.urlshortener.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUrlRequest(
    @NotBlank @Pattern(regexp = "https?://.+", message = "longUrl must be an absolute HTTP(S) URL") String longUrl
) { }
