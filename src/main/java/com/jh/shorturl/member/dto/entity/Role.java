package com.jh.shorturl.member.dto.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("ROLE_USER", "일반 사용자");

    private final String key;
    private final String title;
}
