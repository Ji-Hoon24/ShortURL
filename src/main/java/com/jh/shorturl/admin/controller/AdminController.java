package com.jh.shorturl.admin.controller;

import com.jh.shorturl.admin.service.AdminService;
import com.jh.shorturl.config.JwtConfig;
import com.jh.shorturl.shorter.dto.result.ShorterListResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.jh.shorturl.config.ApiResultUtil.ApiResult;
import static com.jh.shorturl.config.ApiResultUtil.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final JwtConfig jwtConfig;
    @GetMapping("/list")
    public ApiResult<List<ShorterListResult>> getUrlList(HttpServletRequest request) {
        long memberNo = jwtConfig.getMemberNo(request);
        List<ShorterListResult> list = adminService.getUrlList(memberNo);
        return success(list);
    }

}
