package com.example.urlshortener.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import com.example.urlshortener.domain.UrlRepository;
import org.junit.jupiter.api.Test;

class UrlServiceTest {
    @Test void cacheHitAvoidsDatabaseLookupAndRecordsClick() {
        UrlRepository repository = mock(UrlRepository.class);
        UrlCache cache = mock(UrlCache.class);
        ClickTrackingService clicks = mock(ClickTrackingService.class);
        when(cache.get("abc")).thenReturn("https://example.com");
        UrlService service = new UrlService(repository, new Base62Encoder(), cache, clicks);

        assertEquals("https://example.com", service.resolve("abc"));
        verify(repository, never()).findByShortCode(anyString());
        verify(clicks).recordClick("abc");
    }
}
