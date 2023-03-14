package com.jh.shorturl.shorter.controller;

import com.jh.shorturl.config.ApiResultUtil;
import com.jh.shorturl.config.JwtConfig;
import com.jh.shorturl.shorter.dto.entity.Shorter;
import com.jh.shorturl.shorter.dto.request.ShorterRequest;
import com.jh.shorturl.shorter.dto.result.ShorterResult;
import com.jh.shorturl.shorter.service.ShorterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.jh.shorturl.config.ApiResultUtil.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shorter")
public class ShorterController {
    private final ShorterService shorterService;
    private final JwtConfig jwtConfig;

    @PostMapping("/")
    public ApiResult<ShorterResult> shorter(
            @Valid @RequestBody ShorterRequest shorterRequest,
            HttpServletRequest request) {
        long memberNo = jwtConfig.getMemberNo(request);
        ShorterResult result = shorterService.longUrlSave(shorterRequest, memberNo);
        return success(result);
    }

    @GetMapping("/{domain}")
    public ApiResult<String> longUrl(
            @PathVariable String domain
    ) {
        String longUrl = shorterService.getLongUrl(domain);
        return success(longUrl);
    }
}
