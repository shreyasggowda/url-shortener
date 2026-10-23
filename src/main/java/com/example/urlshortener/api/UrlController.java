package com.example.urlshortener.api;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.domain.AppUser;
import com.example.urlshortener.service.UrlService;
import com.example.urlshortener.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlController {
    private final UrlService service;
    private final AnalyticsService analytics;
    public UrlController(UrlService service, AnalyticsService analytics) { this.service = service; this.analytics = analytics; }

    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponse> create(@Valid @RequestBody CreateUrlRequest request, @org.springframework.security.core.annotation.AuthenticationPrincipal AppUser user, HttpServletRequest httpRequest) {
        UrlMapping mapping = service.create(request.longUrl(), request.customAlias(), request.expiresAt(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UrlResponse.from(mapping, baseUrl(httpRequest)));
    }
    @GetMapping("/api/urls")
    public LinkPage links(@org.springframework.security.core.annotation.AuthenticationPrincipal AppUser user, @RequestParam(required = false) String search, @RequestParam(defaultValue = "all") String status, @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        if (page < 0 || size < 1 || size > 50) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 50");
        Page<UrlMapping> results = service.list(user, search, status, from, to, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new LinkPage(results.map(mapping -> UrlResponse.from(mapping, baseUrl(request))).getContent(), results.getNumber(), results.getSize(), results.getTotalElements(), results.getTotalPages());
    }
    @GetMapping("/api/urls/{shortCode}/stats")
    public UrlResponse stats(@PathVariable String shortCode, @org.springframework.security.core.annotation.AuthenticationPrincipal AppUser user, HttpServletRequest request) { return UrlResponse.from(service.stats(shortCode, user), baseUrl(request)); }
    @GetMapping("/api/analytics")
    public AnalyticsService.Dashboard analytics(@org.springframework.security.core.annotation.AuthenticationPrincipal AppUser user, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) { return analytics.dashboard(user, from, to); }
    @GetMapping("/api/urls/{shortCode}/analytics")
    public LinkDetail analytics(@PathVariable String shortCode, @org.springframework.security.core.annotation.AuthenticationPrincipal AppUser user, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, HttpServletRequest request) { return new LinkDetail(UrlResponse.from(service.stats(shortCode, user), baseUrl(request)), analytics.link(user, shortCode, from, to)); }
    @DeleteMapping("/api/urls/{shortCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String shortCode, @org.springframework.security.core.annotation.AuthenticationPrincipal AppUser user) { service.delete(shortCode, user); }
    @GetMapping("/{shortCode:[A-Za-z0-9]+}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(service.resolve(shortCode, request.getHeader("Referer"), request.getHeader("User-Agent"), request.getRemoteAddr()))).build();
    }
    private String baseUrl(HttpServletRequest request) { return request.getRequestURL().toString().replace(request.getRequestURI(), ""); }
    public record LinkPage(java.util.List<UrlResponse> content, int page, int size, long totalElements, int totalPages) { }
    public record LinkDetail(UrlResponse link, AnalyticsService.LinkAnalytics analytics) { }
}
