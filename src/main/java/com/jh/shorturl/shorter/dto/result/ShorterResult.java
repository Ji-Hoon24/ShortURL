package com.jh.shorturl.shorter.dto.result;

import lombok.*;

@Builder
@Getter
public class ShorterResult {

    private String shortUrl;

    private String longUrl;
}
