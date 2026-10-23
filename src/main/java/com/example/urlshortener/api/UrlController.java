package com.example.urlshortener.api;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.domain.AppUser;
import com.example.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlController {
    private final UrlService service;
    public UrlController(UrlService service) { this.service = service; }

    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponse> create(@Valid @RequestBody CreateUrlRequest request, @org.springframework.security.core.annotation.AuthenticationPrincipal AppUser user, HttpServletRequest httpRequest) {
        UrlMapping mapping = service.create(request.longUrl(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UrlResponse.from(mapping, baseUrl(httpRequest)));
    }
    @GetMapping("/api/urls/{shortCode}/stats")
    public UrlResponse stats(@PathVariable String shortCode, @org.springframework.security.core.annotation.AuthenticationPrincipal AppUser user, HttpServletRequest request) { return UrlResponse.from(service.stats(shortCode, user), baseUrl(request)); }
    @GetMapping("/{shortCode:[A-Za-z0-9]+}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(service.resolve(shortCode))).build();
    }
    private String baseUrl(HttpServletRequest request) { return request.getRequestURL().toString().replace(request.getRequestURI(), ""); }
}
