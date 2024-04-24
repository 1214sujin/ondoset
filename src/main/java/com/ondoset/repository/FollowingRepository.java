package com.ondoset.repository;

import com.ondoset.domain.Following;
import com.ondoset.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowingRepository extends JpaRepository<Following, Long> {

	Long countByFollower(Member member);

	@Query("select f from Following f where f.follower=:member order by f.id desc limit 10")
	List<Following> pageFollowing(@Param("member") Member member);

	@Query("select f from Following f where f.follower=:member and f.id<:lastPage order by f.id desc limit 10")
	List<Following> pageFollowing(@Param("member") Member member, @Param("lastPage") Long lastPage);
}
