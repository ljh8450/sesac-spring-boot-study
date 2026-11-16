package com.example.personal_project.presentation.controller;

import com.example.personal_project.application.SimpleShortenUrlService;
import com.example.personal_project.global.exception.handler.GlobalExceptionHandler;
import com.example.personal_project.infrastructure.repository.MapShortenUrlRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ShortenUrlRestControllerTest {

    private static final String ORIGINAL_URL = "https://example.com/slides";
    private static final String CODE = "week03-jpa";

    private AnnotationConfigApplicationContext context;
    private MapShortenUrlRepository repository;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(
                MapShortenUrlRepository.class,
                SimpleShortenUrlService.class,
                ShortenUrlRestController.class);
        repository = context.getBean(MapShortenUrlRepository.class);
        mvc = MockMvcBuilders.standaloneSetup(context.getBean(ShortenUrlRestController.class))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void customCodeRegistrationRedirectAndInformationUseSameLink() throws Exception {
        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(ORIGINAL_URL, CODE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortenUrlKey").value(CODE))
                .andExpect(jsonPath("$.originalUrl").value(ORIGINAL_URL));

        mvc.perform(get("/study/{code}", CODE))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", ORIGINAL_URL))
                .andExpect(header().string("Cache-Control", "no-store"));

        mvc.perform(get("/shortenUrl/{code}", CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectCount").value(1));
        assertEquals(1L, repository.findShortenUrlByShortenUrlKey(CODE)
                .getRedirectCount().longValue());
    }

    @Test
    void customCodeWhitespaceIsRemovedBeforeResponseAndLookup() throws Exception {
        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(ORIGINAL_URL, "  " + CODE + "  ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortenUrlKey").value(CODE));

        mvc.perform(get("/study/{code}", CODE))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", ORIGINAL_URL));
        assertNull(repository.findShortenUrlByShortenUrlKey("  " + CODE + "  "));
    }

    @Test
    void duplicateCustomCodeReturns409AndPreservesExistingLink() throws Exception {
        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(ORIGINAL_URL, CODE)))
                .andExpect(status().isOk());
        mvc.perform(get("/study/{code}", CODE)).andExpect(status().isFound());

        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body("https://example.com/replacement", CODE)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_CODE"))
                .andExpect(jsonPath("$.message").isString());

        assertEquals(ORIGINAL_URL,
                repository.findShortenUrlByShortenUrlKey(CODE).getOriginalUrl());
        assertEquals(1L, repository.findShortenUrlByShortenUrlKey(CODE)
                .getRedirectCount().longValue());
    }

    @Test
    void surroundingWhitespaceCannotBypassCodeUniqueness() throws Exception {
        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(ORIGINAL_URL, CODE)))
                .andExpect(status().isOk());

        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body("https://example.com/replacement", "  " + CODE + "  ")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_CODE"));

        assertNull(repository.findShortenUrlByShortenUrlKey("  " + CODE + "  "));
        assertEquals(ORIGINAL_URL,
                repository.findShortenUrlByShortenUrlKey(CODE).getOriginalUrl());
    }

    @ParameterizedTest
    @MethodSource("invalidCustomCodes")
    void invalidCustomCodeReturnsUniform400(String code) throws Exception {
        mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(body(ORIGINAL_URL, code)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").isString());

        assertNull(repository.findShortenUrlByShortenUrlKey(code));
        assertNull(repository.findShortenUrlByShortenUrlKey(code.trim()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"originalUrl\":\"https://example.com/slides\"}",
            "{\"originalUrl\":\"https://example.com/slides\",\"code\":null}"
    })
    void omittedOrNullCodeStillGeneratesEightCharacterLink(String requestBody) throws Exception {
        var result = mvc.perform(post("/shortenUrl").contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(ORIGINAL_URL))
                .andExpect(jsonPath("$.shortenUrlKey").isString())
                .andReturn();

        String key = JsonPath.read(result.getResponse().getContentAsString(), "$.shortenUrlKey");
        assertTrue(key.matches("[a-zA-Z0-9]{8}"));
        assertNotNull(repository.findShortenUrlByShortenUrlKey(key));
        mvc.perform(get("/study/{code}", key))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", ORIGINAL_URL));
    }

    private static Stream<String> invalidCustomCodes() {
        return Stream.of("", "   ", "ab", "a".repeat(41), "Week03", "-week03",
                "week03-", "week--03", "week/03", "week_03", "회차03", "week 03");
    }

    private String body(String originalUrl, String code) {
        return """
                {"originalUrl":"%s","code":"%s"}
                """.formatted(originalUrl, code);
    }
}
