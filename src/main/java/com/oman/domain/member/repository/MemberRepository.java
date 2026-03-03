package com.oman.domain.member.repository;

import com.oman.domain.member.entity.Member;
import com.oman.domain.member.entity.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findByProviderAndProviderId(SocialProvider provider, String providerId);

    Optional<Member> findByEmail(String email);
}
