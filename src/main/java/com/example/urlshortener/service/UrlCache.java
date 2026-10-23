package com.example.urlshortener.service;

import java.time.Duration;
import java.time.Instant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UrlCache {
    private static final Duration TTL = Duration.ofHours(1);
    private final StringRedisTemplate redis;
    public UrlCache(StringRedisTemplate redis) { this.redis = redis; }
    public String get(String code) { return redis.opsForValue().get("url:" + code); }
    public void put(String code, String longUrl, Instant expiresAt) {
        Duration ttl = expiresAt == null ? TTL : Duration.between(Instant.now(), expiresAt);
        if (ttl.isZero() || ttl.isNegative()) { delete(code); return; }
        redis.opsForValue().set("url:" + code, longUrl, ttl.compareTo(TTL) < 0 ? ttl : TTL);
    }
    public void delete(String code) { redis.delete("url:" + code); }
}
