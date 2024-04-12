package com.ondoset.repository;

import com.ondoset.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	Boolean existsByName(String name);

	Boolean existsByNickname(String nickname);

	Boolean existsByPassword(String password);

	Member findByName(String name);
}
