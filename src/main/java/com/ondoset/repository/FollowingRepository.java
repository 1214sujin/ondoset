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

	@Query("select f from Following f where f.follower=:member order by f.id desc limit 24")
	List<Following> pageFollowing(@Param("member") Member member);

	@Query("select f from Following f where f.follower=:member and f.id<:lastPage order by f.id desc limit 24")
	List<Following> pageFollowing(@Param("member") Member member, @Param("lastPage") Long lastPage);

	@Query("select f from Following f join f.followed m where f.follower=:member and m.nickname like concat('%',:search,'%') order by f.id desc limit 24")
	List<Following> pageFollowingSearch(@Param("member") Member member, @Param("search") String search);

	@Query("select f from Following f join f.followed m where f.follower=:member and m.nickname like concat('%',:search,'%') and f.id<:lastPage order by f.id desc limit 24")
	List<Following> pageFollowingSearch(@Param("member") Member member, @Param("search") String search, @Param("lastPage") Long lastPage);
}
