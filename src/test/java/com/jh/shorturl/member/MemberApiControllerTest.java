package com.jh.shorturl.member;

import com.jh.shorturl.config.JwtConfig;
import com.jh.shorturl.member.controller.MemberApiController;
import com.jh.shorturl.member.dto.entity.Members;
import com.jh.shorturl.redis.RedisService;
import com.jh.shorturl.security.WithMockJwtAuthentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MemberApiControllerTest {
    private MockMvc mockMvc;

    private JwtConfig jwtConfig;

    private RedisService redisService;

    @Autowired
    public void setMockMvc(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Autowired
    public void setJwtConfig(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Autowired
    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Test
    @DisplayName("로그인 성공 테스트 (아이디, 비밀번호가 올바른 경우)")
    void loginSuccessTest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tester@tester.com\",\"passwd\":\"1234\"}")
        );

        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MemberApiController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.response.accessToken").exists())
                .andExpect(jsonPath("$.response.accessToken").isString())
                .andExpect(jsonPath("$.response.refreshToken").exists())
                .andExpect(jsonPath("$.response.refreshToken").isString());
    }

    @Test
    @DisplayName("로그인 실패 테스트 (아이디, 비밀번호가 올바르지 않은 경우)")
    void loginFailureTest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tester@tester.com\",\"passwd\":\"4321\"}")
        );
        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(MemberApiController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.status", is(400)));
    }

    @Test
    @WithMockJwtAuthentication
    @DisplayName("내 정보 조회 성공 테스트 (토큰이 올바른 경우)")
    void myProfileSuccessTest() throws Exception {
        Members members = Members.builder().memberNo(1).build();
        String accessToken = jwtConfig.createAccessToken(members);
        String refreshToken = jwtConfig.createRefreshToken();

        ResultActions result = mockMvc.perform(
                get("/api/member/myProfile")
                    .accept(MediaType.APPLICATION_JSON)
                    .header(jwtConfig.getAccessHeader(), "Bearer " + accessToken)
                    .header(jwtConfig.getRefreshHeader(),"Bearer " + refreshToken)
        );

        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MemberApiController.class))
                .andExpect(handler().methodName("myProfile"))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.response.name", is("tester")))
                .andExpect(jsonPath("$.response.email", is("tester@tester.com")))
                .andExpect(jsonPath("$.response.nickname", is("Tester")))
                .andExpect(jsonPath("$.response.phoneNum", is("010-0000-0000")));
    }

    @Test
    @DisplayName("내 정보 조회 실패 테스트 (토큰이 올바르지 않을 경우)")
    void myProfileFailureTest() throws Exception {
        ResultActions result = mockMvc.perform(
                get("/api/member/myProfile")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(jwtConfig.getAccessHeader(), "Bearer " + randomAlphanumeric(60))
        );
        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.status", is(401)))
                .andExpect(jsonPath("$.error.message", is("토큰이 필요합니다.")));
    }

    @Test
    @DisplayName("비밀번호 재설정 실패(전화번호 인증 전)")
    void passwdResetFailTest() throws Exception {
        ResultActions result = mockMvc.perform(
                put("/api/member/passwdReset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tester@tester.com\",\"newPasswd\":\"qwer1234!@\",\"phoneNum\":\"010-0000-0000\"}")
        );

        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(MemberApiController.class))
                .andExpect(handler().methodName("passwdReset"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.status", is(400)))
                .andExpect(jsonPath("$.error.message", is("전화번호 인증이 필요합니다.")));
    }


    @Test
    @DisplayName("비밀번호 재설정 성공")
    void passwdResetSuccessTest() throws Exception {
        /**
         * Redis에 데이터를 미리 강제로 주입해서 인증이 완료된 것 처럼 보이도록 해둠
         * */
        redisService.savePhoneAuthSuccess("010-0000-0000");

        ResultActions result = mockMvc.perform(
                put("/api/member/passwdReset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"12345@tester.com\",\"newPasswd\":\"qwer1234!@\",\"phoneNum\":\"010-0000-0000\"}")
        );

        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MemberApiController.class))
                .andExpect(handler().methodName("passwdReset"))
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("비밀번호 재설정시 계정 정보가 없는 경우")
    void passwdResetEmailFailTest() throws Exception {
        /**
         * Redis에 데이터를 미리 강제로 주입해서 인증이 완료된 것 처럼 보이도록 해둠
         * */
        redisService.savePhoneAuthSuccess("010-0000-0000");

        ResultActions result = mockMvc.perform(
                put("/api/member/passwdReset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@tester.com\",\"newPasswd\":\"qwer1234!@\",\"phoneNum\":\"010-0000-0000\"}")
        );

        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(MemberApiController.class))
                .andExpect(handler().methodName("passwdReset"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.message", is("이메일 정보가 없습니다.")))
                .andExpect(jsonPath("$.error.status", is(400)));
    }

    @Test
    @DisplayName("회원가입 실패(전화번호 인증 전)")
    void joinFailTest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test1@test.com\",\"passwd\":\"qwer1234!@\",\"phoneNum\":\"010-1234-5678\",\"name\":\"테스터\",\"nickname\":\"테스트닉네임\"}")
        );

        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(MemberApiController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.status", is(400)))
                .andExpect(jsonPath("$.error.message", is("전화번호 인증이 필요합니다.")));
    }

    @Test
    @DisplayName("회원가입 실패(이메일 중복)")
    void joinEmailFailTest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tester@tester.com\",\"passwd\":\"qwer1234!@\",\"phoneNum\":\"010-1234-5678\",\"name\":\"테스터\",\"nickname\":\"테스트닉네임\"}")
        );

        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(MemberApiController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.status", is(400)))
                .andExpect(jsonPath("$.error.message", is("중복된 이메일이 있습니다.")));
    }

    @Test
    @DisplayName("회원가입 실패(닉네임 중복)")
    void joinNicknameFailTest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test2@test.com\",\"passwd\":\"qwer1234!@\",\"phoneNum\":\"010-1234-5678\",\"name\":\"Tester\",\"nickname\":\"Tester\"}")
        );

        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(MemberApiController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.status", is(400)))
                .andExpect(jsonPath("$.error.message", is("중복된 닉네임이 있습니다.")));
    }

    @Test
    @DisplayName("회원가입 실패(전화번호 중복)")
    void joinPhoneNumFailTest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test55@test.com\",\"passwd\":\"qwer1234!@\",\"phoneNum\":\"010-0000-0000\",\"name\":\"Tester1\",\"nickname\":\"Tester1\"}")
        );

        result.andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(MemberApiController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.status", is(400)))
                .andExpect(jsonPath("$.error.message", is("중복된 전화번호가 있습니다.")));
    }

    @Test
    @DisplayName("회원가입 성공")
    void joinSuccessTest() throws Exception {
        /**
         * Redis에 데이터를 미리 강제로 주입해서 인증이 완료된 것 처럼 보이도록 해둠
         * */
        redisService.savePhoneAuthSuccess("010-1234-5678");

        ResultActions result = mockMvc.perform(
                post("/api/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test3@test.com\",\"passwd\":\"qwer1234!@\",\"phoneNum\":\"010-1234-5678\",\"name\":\"테스터\",\"nickname\":\"테스트닉네임\"}")
        );

        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(MemberApiController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(jsonPath("$.success", is(true)));
    }
}
