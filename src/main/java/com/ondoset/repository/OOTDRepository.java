package com.ondoset.repository;

import com.ondoset.domain.Member;
import com.ondoset.domain.OOTD;
import com.ondoset.dto.ootd.OotdDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OOTDRepository extends JpaRepository<OOTD, Long> {

	Long countByMember(Member member);

	@Query("select new com.ondoset.dto.ootd.OotdDTO(o.id, (o.departTime/86400+1)*86400-:timezone/1000 date, o.lowestTemp, o.highestTemp, o.imageURL) " +
			"from OOTD o where o.member=:member and o.reportedCount<5 and o.isBlinded=false order by o.id desc limit 10")
	List<OotdDTO> pageMyProfile(@Param("timezone") int timezone, @Param("member") Member member);

	@Query("select new com.ondoset.dto.ootd.OotdDTO(o.id, (o.departTime/86400+1)*86400-:timezone/1000 date, o.lowestTemp, o.highestTemp, o.imageURL) " +
			"from OOTD o where o.member=:member and o.reportedCount<5 and o.isBlinded=false and o.id<:lastPage order by o.id desc limit 10")
	List<OotdDTO> pageMyProfile(@Param("timezone") int timezone, @Param("member") Member member, @Param("lastPage") Long lastPage);
}
