package com.jh.shorturl.shorter.dto.result;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
public class ShorterListResult {
    private long shorterNo;
    private String longUrl;
    private String shortUrl;
    private long viewCnt;
}
