package com.jh.shorturl.member.domain;


import com.jh.shorturl.member.dto.entity.Members;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
public interface MemberRepository extends JpaRepository<Members, Long> {

    Optional<Members> findByEmail(String email);

    Optional<Members> findByNickname(String nickname);

    Optional<Members> findByPhoneNum(String phoneNum);

    List<Members> findByEmailOrNicknameOrPhoneNum(String email, String nickname, String phoneNum);
    Optional<Members> findByMemberNo(long memberNo);

}
