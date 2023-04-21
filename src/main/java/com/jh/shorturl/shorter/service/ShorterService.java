package com.jh.shorturl.shorter.service;

import com.jh.shorturl.common.Base62;
import com.jh.shorturl.redis.RedisService;
import com.jh.shorturl.shorter.domain.ShorterRepository;
import com.jh.shorturl.shorter.dto.entity.Shorter;
import com.jh.shorturl.shorter.dto.request.ShorterRequest;
import com.jh.shorturl.shorter.dto.result.ShorterResult;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShorterService {
    private final ShorterRepository shorterRepository;

    private final RedisService redisService;

    @Value("${domain}")
    private String domainUrl;

    @Transactional(rollbackFor = Exception.class)
    public ShorterResult longUrlSave(ShorterRequest shorterRequest, long memberNo) {
        Optional<Shorter> longEntity = shorterRepository.findByLongUrl(shorterRequest.getLongUrl());
        if(longEntity.isPresent()) {
            return ShorterResult.builder()
                    .shortUrl(longEntity.get().getShortUrl())
                    .build();
        }

        String longUrl = shorterRequest.getLongUrl();

        Shorter entity = Shorter.builder()
                .longUrl(longUrl)
                .viewCnt(0)
                .memberNo(memberNo)
                .build();

        Shorter saveEntity = shorterRepository.save(entity);

        String shortUrl = this.longUrlToShortUrl(saveEntity.getShorterNo(), shorterRequest.getRequestShortUrl());
        saveEntity.changeShortUrl(shortUrl);
        shorterRepository.save(entity);

        redisService.saveShortUrl(shortUrl, longUrl);

        return ShorterResult.builder()
                .shortUrl(saveEntity.getShortUrl())
                .build();
    }

    private String longUrlToShortUrl(long shorterNo, String requestShortUrl) {
        if(requestShortUrl != null && !requestShortUrl.equals("")) {
            return domainUrl + requestShortUrl;
        }
        String shorten = Base62.encoding(shorterNo);
        return domainUrl + shorten;
    }

    public String getLongUrl(String domain) {
        String redisLongUrl = redisService.findLongUrl(domainUrl + domain);
        if(!StringUtil.isNullOrEmpty(redisLongUrl)) {
            this.plusUrlViewCnt(domain);
            return redisLongUrl;
        }

        Shorter entity = shorterRepository.findByShortUrl(domainUrl + domain).orElseThrow(() -> new IllegalArgumentException("해당 URL 정보가 없습니다."));
        redisService.saveShortUrl(entity.getShortUrl(), entity.getLongUrl());
        this.plusUrlViewCnt(entity);
        return entity.getLongUrl();
    }

    @Async
    protected void plusUrlViewCnt(String domain) {
        Shorter entity = shorterRepository.findByShortUrl(domainUrl + domain).orElseThrow(() -> new IllegalArgumentException("해당 URL 정보가 없습니다."));
        this.plusUrlViewCnt(entity);
    }

    private void plusUrlViewCnt(Shorter entity) {
        entity.plusViewCnt();
        shorterRepository.save(entity);
    }
}
