package com.jh.shorturl.member.controller;

import com.jh.shorturl.config.ApiResultUtil.ApiResult;
import com.jh.shorturl.exception.UnauthorizedException;
import com.jh.shorturl.member.dto.request.JoinRequest;
import com.jh.shorturl.member.dto.request.LoginRequest;
import com.jh.shorturl.member.dto.request.PasswdResetRequest;
import com.jh.shorturl.member.dto.result.LoginResult;
import com.jh.shorturl.member.dto.result.MyProfileResult;
import com.jh.shorturl.member.service.MemberService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.jh.shorturl.config.ApiResultUtil.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member")
public class MemberApiController {

    private final MemberService memberService;

    @ApiOperation(value = "회원가입(전화번호 인증 필수)")
    @PostMapping("/join")
    public ApiResult<Boolean> join(@Valid @RequestBody JoinRequest joinRequest) {
        memberService.join(joinRequest);
        return success();
    }

    @ApiOperation(value = "로그인")
    @PostMapping("/login")
    public ApiResult<LoginResult> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResult result = memberService.login(loginRequest);

        return success(result);
    }


    @ApiOperation(value = "비밀번호 재설정(전화번호 인증 필수)")
    @PutMapping("/passwdReset")
    public ApiResult<Boolean> passwdReset(@Valid @RequestBody PasswdResetRequest request) {
        memberService.passwdReset(request);
        return success();
    }

    @ApiOperation(value = "내 프로필 조회(엑세스 토큰 필수)")
    @GetMapping("/myProfile")
    public ApiResult<MyProfileResult> myProfile(@ApiParam(hidden = true) @AuthenticationPrincipal String SMemberNo) {
        long memberNo = 0;
        try {
            memberNo = Long.parseLong(SMemberNo);
        } catch (Exception e) {
            throw new UnauthorizedException("토큰이 필요합니다.");
        }
        MyProfileResult result = memberService.myProfile(memberNo);

        return success(result);
    }
}
