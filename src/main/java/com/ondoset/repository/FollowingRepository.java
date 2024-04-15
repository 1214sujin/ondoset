package com.ondoset.repository;

import com.ondoset.domain.Following;
import com.ondoset.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowingRepository extends JpaRepository<Following, Long> {

	Long countByFollower(Member member);
}
