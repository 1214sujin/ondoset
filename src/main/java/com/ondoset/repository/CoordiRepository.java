package com.ondoset.repository;

import com.ondoset.domain.Coordi;
import com.ondoset.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoordiRepository extends JpaRepository<Coordi, Long> {

	Boolean existsByConsistings_Clothes_MemberAndDate(Member member, Long date);

	Optional<Coordi> findByConsistings_Clothes_MemberAndDate(Member member, Long date);

	Boolean existsByIdAndConsistings_Clothes_Member(Long coordiId, Member member);

	@Query("select distinct cd.id " +
			"from Coordi cd left join cd.consistings cs left join cs.clothes ct " +
			"where ct.member=:member and function('date_format', from_unixtime(cd.date), '%Y%m')=function('date_format', concat(:year, :month, '01'), '%Y%m')")
	List<Long> findByMemberAndMonth(@Param("member") Member member, @Param("year") int year, @Param("month") String month);
}
