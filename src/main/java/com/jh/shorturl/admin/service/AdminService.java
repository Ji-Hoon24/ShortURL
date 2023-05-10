package com.jh.shorturl.admin.service;

import com.jh.shorturl.common.Base62;
import com.jh.shorturl.redis.RedisService;
import com.jh.shorturl.shorter.domain.ShorterRepository;
import com.jh.shorturl.shorter.dto.entity.Shorter;
import com.jh.shorturl.shorter.dto.request.ShorterRequest;
import com.jh.shorturl.shorter.dto.result.ShorterListResult;
import com.jh.shorturl.shorter.dto.result.ShorterResult;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final ShorterRepository shorterRepository;
    public List<ShorterListResult> getUrlList(long memberNo) {
        return shorterRepository.findByMemberNo(memberNo);
    }
}
