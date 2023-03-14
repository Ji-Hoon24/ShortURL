package com.jh.shorturl.shorter.service;

import com.jh.shorturl.common.Base62;
import com.jh.shorturl.shorter.domain.ShorterRepository;
import com.jh.shorturl.shorter.dto.entity.Shorter;
import com.jh.shorturl.shorter.dto.request.ShorterRequest;
import com.jh.shorturl.shorter.dto.result.ShorterResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShorterService {
    private final ShorterRepository shorterRepository;

    @Value("${domain}")
    private String domainUrl;

    @Transactional(rollbackFor = Exception.class)
    public ShorterResult longUrlSave(ShorterRequest shorterRequest, long memberNo) {
        Optional<Shorter> longEntity = shorterRepository.findByLongUrl(shorterRequest.getLongUrl());
        if(longEntity.isPresent()) {
            ShorterResult result = ShorterResult.builder()
                    .shortUrl(longEntity.get().getShortUrl())
                    .build();
            return result;
        }

        Shorter entity = Shorter.builder()
                .longUrl(shorterRequest.getLongUrl())
                .viewCnt(0)
                .memberNo(memberNo)
                .build();

        Shorter saveEntity = shorterRepository.save(entity);

        String shortUrl = this.longUrlToShortUrl(saveEntity.getShorterNo(), shorterRequest.getRequestShortUrl());
        saveEntity.changeShortUrl(shortUrl);
        shorterRepository.save(entity);

        ShorterResult result = ShorterResult.builder()
                .shortUrl(saveEntity.getShortUrl())
                .build();
        return result;
    }

    private String longUrlToShortUrl(long shorterNo, String requestShortUrl) {
        if(requestShortUrl != null && !requestShortUrl.equals("")) {
            return domainUrl + requestShortUrl;
        }
        String shorten = Base62.encoding(shorterNo);
        return domainUrl + shorten;
    }

    public String getLongUrl(String domain) {
        Shorter entity = shorterRepository.findByShortUrl(domainUrl + domain).orElseThrow(() -> new IllegalArgumentException("해당 URL 정보가 없습니다."));
        entity.plusViewCnt();
        shorterRepository.save(entity);
        return entity.getLongUrl();
    }
}
