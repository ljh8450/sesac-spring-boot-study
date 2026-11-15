package com.example.personal_project.application;

import com.example.personal_project.domain.ShortenUrl;
import com.example.personal_project.domain.ShortenUrlRepository;
import com.example.personal_project.global.exception.LackOfShortenUrlKeyException;
import com.example.personal_project.global.exception.NotFoundShortenException;
import com.example.personal_project.infrastructure.repository.MapShortenUrlRepository;
import com.example.personal_project.presentation.dto.url.ShortenUrlCreateRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleShortenUrlServiceTest {

    private static final String ORIGINAL_URL = "https://example.com/slides";
    private static final String KEY = "Ab23Cd45";

    private MapShortenUrlRepository repository;
    private SimpleShortenUrlService service;

    @BeforeEach
    void setUp() {
        repository = new MapShortenUrlRepository();
        service = new SimpleShortenUrlService(repository);
    }

    @Test
    void registrationSavesOriginalUrlAndCreatesEightCharacterKey() {
        var response = service.generateShortenUrl(request());

        String key = response.getShortenUrlKey();
        assertNotNull(key);
        assertEquals(8, key.length());
        assertEquals(ORIGINAL_URL, response.getOriginalUrl());

        ShortenUrl saved = repository.findShortenUrlByShortenUrlKey(key);
        assertNotNull(saved);
        assertEquals(key, saved.getShortenUrlKey());
        assertEquals(ORIGINAL_URL, saved.getOriginalUrl());
        assertEquals(0L, saved.getRedirectCount().longValue());
    }

    @Test
    void informationLookupDoesNotIncreaseRedirectCount() {
        seed();

        var first = service.getShortenUrlInformationByShortenUrlKey(KEY);
        var second = service.getShortenUrlInformationByShortenUrlKey(KEY);

        assertEquals(ORIGINAL_URL, first.getOriginalUrl());
        assertEquals(KEY, first.getShortenUrlKey());
        assertEquals(0L, first.getRedirectCount().longValue());
        assertEquals(0L, second.getRedirectCount().longValue());
        assertEquals(0L, repository.findShortenUrlByShortenUrlKey(KEY)
                .getRedirectCount().longValue());
    }

    @Test
    void originalUrlLookupIncreasesRedirectCountEachTime() {
        seed();

        assertEquals(ORIGINAL_URL, service.getOriginalUrlByShortenUrlKey(KEY));
        assertEquals(1L, service.getShortenUrlInformationByShortenUrlKey(KEY)
                .getRedirectCount().longValue());

        assertEquals(ORIGINAL_URL, service.getOriginalUrlByShortenUrlKey(KEY));
        assertEquals(2L, service.getShortenUrlInformationByShortenUrlKey(KEY)
                .getRedirectCount().longValue());
    }

    @Test
    void missingKeyThrowsNotFoundException() {
        assertThrows(NotFoundShortenException.class,
                () -> service.getShortenUrlInformationByShortenUrlKey("missing"));
        assertThrows(NotFoundShortenException.class,
                () -> service.getOriginalUrlByShortenUrlKey("missing"));
    }

    @Test
    void fiveCollisionsFailWithoutSaving() {
        ShortenUrlRepository occupiedRepository = mock(ShortenUrlRepository.class);
        when(occupiedRepository.findShortenUrlByShortenUrlKey(anyString()))
                .thenReturn(new ShortenUrl(ORIGINAL_URL, KEY));

        var collisionService = new SimpleShortenUrlService(occupiedRepository);

        assertThrows(LackOfShortenUrlKeyException.class,
                () -> collisionService.generateShortenUrl(request()));

        verify(occupiedRepository, times(5)).findShortenUrlByShortenUrlKey(anyString());
        verify(occupiedRepository, never()).saveShortenUrl(any(ShortenUrl.class));
    }

    private void seed() {
        repository.saveShortenUrl(new ShortenUrl(ORIGINAL_URL, KEY));
    }

    private ShortenUrlCreateRequestDto request() {
        var request = mock(ShortenUrlCreateRequestDto.class);
        when(request.getOriginalUrl()).thenReturn(ORIGINAL_URL);
        return request;
    }
}
