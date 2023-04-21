package com.jh.shorturl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebMvc
public class SwaggerConfig {

    private ApiInfo shortURLInfo() {
        return new ApiInfoBuilder().title("ShortURL API")
                .description("ShortURL Docs").build();
    }

    @Bean
    public Docket authApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("인증")
                .securityContexts(Arrays.asList(securityContext()))
                .securitySchemes(Arrays.asList(accessToken(), refreshToken()))
                .apiInfo(this.shortURLInfo())
                .select()
                .apis(RequestHandlerSelectors
                        .basePackage("com.jh.shorturl.auth"))
                .paths(PathSelectors.ant("/**"))
                .build();
    }

    @Bean
    public Docket memberApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("회원")
                .securityContexts(Arrays.asList(securityContext()))
                .securitySchemes(Arrays.asList(accessToken(), refreshToken()))
                .apiInfo(this.shortURLInfo())
                .select()
                .apis(RequestHandlerSelectors
                        .basePackage("com.jh.shorturl.member"))
                .paths(PathSelectors.ant("/**"))
                .build();
    }

    @Bean
    public Docket shorterApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("단축기")
                .securityContexts(Arrays.asList(securityContext()))
                .securitySchemes(Arrays.asList(accessToken(), refreshToken()))
                .apiInfo(this.shortURLInfo())
                .select()
                .apis(RequestHandlerSelectors
                        .basePackage("com.jh.shorturl.shorter"))
                .paths(PathSelectors.ant("/**"))
                .build();
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(accessTokenAuth())
                .securityReferences(refreshTokenAuth())
                .build();
    }

    private List<SecurityReference> accessTokenAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(new SecurityReference("AccessToken", authorizationScopes));
    }

    private List<SecurityReference> refreshTokenAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(new SecurityReference("RefreshToken", authorizationScopes));
    }

    private ApiKey accessToken() {
        return new ApiKey("AccessToken", "X-AUTH-TOKEN", "header");
    }

    private ApiKey refreshToken() {
        return new ApiKey("RefreshToken", "X-REFRESH-TOKEN", "header");
    }
}