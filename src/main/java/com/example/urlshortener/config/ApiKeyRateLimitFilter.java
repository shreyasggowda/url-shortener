package com.example.urlshortener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyRateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService rateLimit;
    private final ObjectMapper objectMapper;
    public ApiKeyRateLimitFilter(RateLimitService rateLimit, ObjectMapper objectMapper) { this.rateLimit = rateLimit; this.objectMapper = objectMapper; }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) { return !("POST".equals(request.getMethod()) && "/api/urls".equals(request.getRequestURI())); }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String key = request.getHeader("X-API-Key");
        if (key == null || key.isBlank()) { reject(response, HttpStatus.UNAUTHORIZED, "X-API-Key header is required"); return; }
        if (!rateLimit.allow(key)) { reject(response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"); return; }
        chain.doFilter(request, response);
    }
    private void reject(HttpServletResponse response, HttpStatus status, String error) throws IOException { response.setStatus(status.value()); response.setContentType(MediaType.APPLICATION_JSON_VALUE); objectMapper.writeValue(response.getWriter(), Map.of("error", error)); }
}
