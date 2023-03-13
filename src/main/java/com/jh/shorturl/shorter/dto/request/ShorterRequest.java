package com.jh.shorturl.shorter.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
public class ShorterRequest {
    @Schema(description = "단축하려고 하는 긴 URL", example = "https://github.com/Ji-Hoon24/ShortURL")
    @NotBlank(message = "URL은 필수 입력 값입니다.")
    private String longUrl;

    @Schema(description = "사용하고 싶은 URL(기존 데이터와 중복 불가능)", example = "Event")
    private String requestShortUrl;
}
