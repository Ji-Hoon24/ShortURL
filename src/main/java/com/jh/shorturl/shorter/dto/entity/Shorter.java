package com.jh.shorturl.shorter.dto.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Shorter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "shorter_seq")
    @Schema(description = "데이터의 고유값(PK)")
    private long shorterNo;

    @Schema(description = "요청한 유저 정보")
    private long memberNo;

    @Schema(description = "단축할 긴 URL값")
    private String longUrl;

    @Schema(description = "단축된 짧은 URL값")
    private String shortUrl;

    @Schema(description = "해당 url에 접근 한 횟수")
    private long viewCnt;

    public void changeShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
}
