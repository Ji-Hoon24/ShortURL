package com.jh.shorturl.auth.controller;

import com.jh.shorturl.auth.dto.request.SendAuthRequest;
import com.jh.shorturl.auth.dto.request.ValidAuthRequest;
import com.jh.shorturl.auth.dto.result.AuthResult;
import com.jh.shorturl.auth.service.AuthService;
import com.jh.shorturl.config.ApiResultUtil.ApiResult;
import com.jh.shorturl.member.dto.result.LoginResult;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.jh.shorturl.config.ApiResultUtil.*;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthApiController {

    private final AuthService authService;

    @ApiOperation(value = "인증번호 발급 받기")
    @PostMapping("/sendAuth")
    public ApiResult<AuthResult> sendAuth(@Valid @RequestBody SendAuthRequest sendAuthRequest) {
        AuthResult result = authService.sendAuth(sendAuthRequest);
        return success(result);
    }

    @ApiOperation(value = "인증번호 확인")
    @PostMapping("/validAuth")
    public ApiResult<Boolean> validAuth(@Valid @RequestBody ValidAuthRequest validAuthRequest) {
        boolean result = authService.validAuth(validAuthRequest);
        return success(result);
   }

    @ApiOperation(value = "리프레시 토큰으로 토큰 재발급(엑세스 토큰 및 리프레시 토큰 필수)")
    @PostMapping("/refresh")
    public ApiResult<LoginResult> refresh(HttpServletRequest request) {
        LoginResult result = authService.newAccessToken(request);
        return success(result);
    }
}
