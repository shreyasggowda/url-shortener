package com.example.urlshortener.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
    private static final String SCRIPT = """
        local current = redis.call('HMGET', KEYS[1], 'tokens', 'updated')
        local now = redis.call('TIME'); local nowMs = now[1] * 1000 + math.floor(now[2] / 1000)
        local capacity = tonumber(ARGV[1]); local refillPerMs = tonumber(ARGV[2]); local ttl = tonumber(ARGV[3])
        local tokens = tonumber(current[1]) or capacity; local updated = tonumber(current[2]) or nowMs
        tokens = math.min(capacity, tokens + (nowMs - updated) * refillPerMs)
        if tokens < 1 then redis.call('HMSET', KEYS[1], 'tokens', tokens, 'updated', nowMs); redis.call('PEXPIRE', KEYS[1], ttl); return 0 end
        redis.call('HMSET', KEYS[1], 'tokens', tokens - 1, 'updated', nowMs); redis.call('PEXPIRE', KEYS[1], ttl); return 1
        """;
    private final StringRedisTemplate redis;
    private final int capacity;
    private final long windowMs;
    public RateLimitService(StringRedisTemplate redis, @Value("${app.rate-limit.capacity:100}") int capacity, @Value("${app.rate-limit.window-seconds:60}") long windowSeconds) { this.redis = redis; this.capacity = capacity; this.windowMs = windowSeconds * 1000; }
    public boolean allow(String apiKey) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(SCRIPT, Long.class);
        Long result = redis.execute(script, List.of("rate-limit:" + apiKey), String.valueOf(capacity), String.valueOf((double) capacity / windowMs), String.valueOf(windowMs));
        return Long.valueOf(1L).equals(result);
    }
}
