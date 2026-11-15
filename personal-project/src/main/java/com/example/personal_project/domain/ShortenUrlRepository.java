package com.example.personal_project.domain;

public interface ShortenUrlRepository {
    void saveShortenUrl(ShortenUrl shortenUrl);
    ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey);
}
