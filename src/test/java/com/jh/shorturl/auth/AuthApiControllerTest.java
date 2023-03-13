package com.jh.shorturl.auth;

import com.jh.shorturl.auth.controller.AuthApiController;
import com.jh.shorturl.config.JwtConfig;
import com.jh.shorturl.member.dto.entity.Members;
import com.jh.shorturl.redis.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthApiControllerTest {

    private MockMvc mockMvc;

    private RedisService redisService;

    private JwtConfig jwtConfig;

    @Autowired
    public void setMockMvc(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Autowired
    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Autowired
    public void setJwtConfig(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Test
    @DisplayName("인증번호 발급 실패(전화번호 없음)")
    void sendAuthPhoneFailTest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/auth/sendAuth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{}")
        );

        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(AuthApiController.class))
                .andExpect(handler().methodName("sendAuth"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.message", is("전화번호는 필수 입력 값입니다.")))
                .andExpect(jsonPath("$.error.status", is(400)));
    }

    @Test
    @DisplayName("인증번호 발급 성공")
    void sendAuthSuccessTest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/auth/sendAuth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNum\":\"010-1234-5768\"}")
        );

        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(AuthApiController.class))
                .andExpect(handler().methodName("sendAuth"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.response.authCode").exists())
                .andExpect(jsonPath("$.response.authCode").isString());
    }

    @Test
    @DisplayName("인증번호 확인 실패(전화번호 없음)")
    void validAuthPhoneFailTest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/auth/validAuth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"authCode\":\"5157\"}")
        );

        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(AuthApiController.class))
                .andExpect(handler().methodName("validAuth"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.message", is("전화번호는 필수 입력 값입니다.")))
                .andExpect(jsonPath("$.error.status", is(400)));
    }

    @Test
    @DisplayName("인증번호 확인 실패(인증번호 없음)")
    void validAuthAuthCodeFailTest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/auth/validAuth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNum\":\"010-1234-5768\"}")
        );

        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(AuthApiController.class))
                .andExpect(handler().methodName("validAuth"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.message", is("인증번호는 필수 입력 값입니다.")))
                .andExpect(jsonPath("$.error.status", is(400)));
    }

    @Test
    @DisplayName("인증번호 확인 실패(인증번호 불일치)")
    void validAuthAuthCodeNoValidFailTest() throws Exception {
        redisService.savePhoneAuthCode("010-5678-1234", "7515");

        ResultActions result = mockMvc.perform(
                post("/api/auth/validAuth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNum\":\"010-5678-1234\", \"authCode\":\"4678\"}")
        );

        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(AuthApiController.class))
                .andExpect(handler().methodName("validAuth"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.response", is(false)));

        redisService.delete("010-5678-1234");
    }

    @Test
    @DisplayName("인증번호 확인 성공")
    void validAuthSuccessTest() throws Exception {
        redisService.savePhoneAuthCode("010-3333-4444", "5789");

        ResultActions result = mockMvc.perform(
                post("/api/auth/validAuth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNum\":\"010-3333-4444\", \"authCode\":\"5789\"}")
        );

        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(AuthApiController.class))
                .andExpect(handler().methodName("validAuth"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.response", is(true)));
        redisService.delete("010-3333-4444");
    }

    @Test
    @DisplayName("리프레시 토큰 발급 실패(리프레시 토큰 없음)")
    void refreshNonRefreshTokenFailTest() throws Exception {
        Members members = Members.builder().memberNo(1).build();
        String accessToken = jwtConfig.createAccessToken(members);
        ResultActions result = mockMvc.perform(
                post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(jwtConfig.getAccessHeader(),"Bearer " + accessToken)
        );

        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(AuthApiController.class))
                .andExpect(handler().methodName("refresh"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.status", is(401)))
                .andExpect(jsonPath("$.error.message", is("리프레시 토큰이 없습니다. 로그인이 필요합니다.")));
    }

    @Test
    @DisplayName("리프레시 토큰 발급 실패(엑세스 토큰 없음)")
    void refreshNonAccessTokenFailTest() throws Exception {
        String refreshToken = jwtConfig.createRefreshToken();
        redisService.saveRefreshToken(2, refreshToken);

        ResultActions result = mockMvc.perform(
                post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(jwtConfig.getRefreshHeader(),"Bearer " + refreshToken)
        );

        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(AuthApiController.class))
                .andExpect(handler().methodName("refresh"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.status", is(401)))
                .andExpect(jsonPath("$.error.message", is("엑세스 토큰이 필요합니다.")));
        redisService.delete("2");
    }

    @Test
    @DisplayName("리프레시 토큰으로 엑세스 토큰 재발급 성공")
    void refreshSuccessTest() throws Exception {
        Members members = Members.builder().memberNo(1).build();
        String accessToken = jwtConfig.createAccessToken(members);
        String refreshToken = jwtConfig.createRefreshToken();
        redisService.saveRefreshToken(1, refreshToken);

        ResultActions result = mockMvc.perform(
                post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(jwtConfig.getAccessHeader(), "Bearer " + accessToken)
                        .header(jwtConfig.getRefreshHeader(),"Bearer " + refreshToken)
        );

        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(AuthApiController.class))
                .andExpect(handler().methodName("refresh"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.response.accessToken").exists())
                .andExpect(jsonPath("$.response.accessToken").isString())
                .andExpect(jsonPath("$.response.refreshToken").exists())
                .andExpect(jsonPath("$.response.refreshToken").isString());

        redisService.delete("1");
    }

}
