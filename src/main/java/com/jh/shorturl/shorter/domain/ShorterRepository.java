package com.jh.shorturl.shorter.domain;

import com.jh.shorturl.shorter.dto.entity.Shorter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@EnableJpaRepositories
public interface ShorterRepository extends JpaRepository<Shorter, Long> {

    Optional<Shorter> findByShortUrl(String shortUrl);

    Optional<Shorter> findByLongUrl(String longUrl);
}
