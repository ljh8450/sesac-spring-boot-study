package com.example.personal_project.application;

import com.example.personal_project.domain.ShortenUrl;
import com.example.personal_project.domain.ShortenUrlRepository;
import com.example.personal_project.global.exception.LackOfShortenUrlKeyException;
import com.example.personal_project.global.exception.NotFoundShortenException;
import com.example.personal_project.presentation.dto.url.ShortenUrlCreateRequestDto;
import com.example.personal_project.presentation.dto.url.ShortenUrlCreateResponseDto;
import com.example.personal_project.presentation.dto.url.ShortenUrlInformationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimpleShortenUrlService {

    private ShortenUrlRepository shortenUrlRepository;

    @Autowired
    SimpleShortenUrlService(ShortenUrlRepository shortenUrlRepository) {
        this.shortenUrlRepository = shortenUrlRepository;
    }

    public ShortenUrlCreateResponseDto generateShortenUrl(
            ShortenUrlCreateRequestDto shortenUrlCreateRequestDto
    ) {
        String originalUrl = shortenUrlCreateRequestDto.getOriginalUrl();
        final int MAX_ATTEMPTS = 5;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String key = ShortenUrl.generateShortenUrlKey();
            ShortenUrl candidate = new ShortenUrl(originalUrl, key);

            boolean saved = shortenUrlRepository.saveShortenUrlIfAbsent(candidate);

            if (saved) {
                return new ShortenUrlCreateResponseDto(candidate);
            }
        }

        throw new LackOfShortenUrlKeyException("URL 생성에 실패했습니다.");
    }

    private String getUniqueShortenUrlKEty() {
        final int MAX_RETRY_COUNT = 5;
        int count = 0;

        while (count++ < MAX_RETRY_COUNT) {
            String shortenUrlKey = ShortenUrl.generateShortenUrlKey();
            ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

            if (null == shortenUrl) return shortenUrlKey;
        }

        throw new LackOfShortenUrlKeyException("URL 생성에 실패했습니다.");
    }

    public ShortenUrlInformationDto getShortenUrlInformationByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if (null == shortenUrl) throw new NotFoundShortenException("해당 URL이 등록되어있지 않습니다.");

        ShortenUrlInformationDto shortenUrlInformationDto = new ShortenUrlInformationDto(shortenUrl);

        return shortenUrlInformationDto;
    }

    public String getOriginalUrlByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.increaseRedirectCount(shortenUrlKey);

        if (null == shortenUrl) throw new NotFoundShortenException("해당 URL이 등록되어있지 않습니다.");

        String originalUrl = shortenUrl.getOriginalUrl();

        return originalUrl;
    }

}
