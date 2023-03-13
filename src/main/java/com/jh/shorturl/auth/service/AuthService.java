package com.jh.shorturl.auth.service;

import com.jh.shorturl.auth.dto.request.SendAuthRequest;
import com.jh.shorturl.auth.dto.request.ValidAuthRequest;
import com.jh.shorturl.auth.dto.result.AuthResult;
import com.jh.shorturl.config.JwtConfig;
import com.jh.shorturl.exception.UnauthorizedException;
import com.jh.shorturl.member.dto.entity.Members;
import com.jh.shorturl.member.dto.result.LoginResult;
import com.jh.shorturl.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtConfig jwtConfig;

    private final RedisService redisService;

    @Transactional(rollbackFor = Exception.class)
    public LoginResult newAccessToken(HttpServletRequest request) {
        String accessToken = jwtConfig.extractAccessToken(request).orElseThrow(
                () -> new UnauthorizedException("엑세스 토큰이 필요합니다.")
        );
        String refreshToken = jwtConfig.extractRefreshToken(request).orElseThrow(
                () -> new UnauthorizedException("리프레시 토큰이 없습니다. 로그인이 필요합니다.")
        );

        Optional<String> SMemberNo = jwtConfig.extractMemberNo(accessToken);
        long memberNo = 0;
        if(SMemberNo.isPresent()) {
            memberNo = Long.parseLong(SMemberNo.get());
        }

        String redisRefreshToken = redisService.findRefreshToken(memberNo);

        if(refreshToken.equals(redisRefreshToken) && jwtConfig.isTokenValid(refreshToken)) {
            Members members = Members.builder().memberNo(memberNo).build();
            LoginResult result = redisService.tokenCreate(members);
            return result;
        }
        throw new UnauthorizedException("로그인이 필요합니다.");
    }

    @Transactional(rollbackFor = Exception.class)
    public AuthResult sendAuth(SendAuthRequest request) {
        String authCode = this.randomAuthCode();
        redisService.savePhoneAuthCode(request.getPhoneNum(), authCode);

        return AuthResult.builder().authCode(authCode).build();
    }

    private String randomAuthCode() {
        Random rand = new Random();
        String numStr = "";
        for (int i = 0; i < 4; i++) {
            String ran = Integer.toString(rand.nextInt(10));
            numStr += ran;
        }
        return numStr;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean validAuth(ValidAuthRequest request) {
        String redisAuthCode = redisService.findPhoneAuthCode(request.getPhoneNum());
        if(redisAuthCode == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }
        if(redisAuthCode.equals(request.getAuthCode())) {
            redisService.delete(request.getPhoneNum());
            redisService.savePhoneAuthSuccess(request.getPhoneNum());
            return true;
        }

        return false;
    }

}
