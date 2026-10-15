package com.example.urlshortener.service;

import com.example.urlshortener.domain.UrlRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClickTrackingService {
    private final UrlRepository repository;
    public ClickTrackingService(UrlRepository repository) { this.repository = repository; }
    @Async
    @Transactional
    public void recordClick(String shortCode) { repository.incrementClickCount(shortCode); }
}
