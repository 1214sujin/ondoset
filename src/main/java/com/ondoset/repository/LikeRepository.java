package com.ondoset.repository;

import com.ondoset.domain.Like;
import com.ondoset.domain.Member;
import com.ondoset.domain.OOTD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

	Long countByMember(Member member);

	Boolean existsByMemberAndOotd(Member member, OOTD ootd);
}
