package com.ondoset.repository;

import com.ondoset.domain.Enum.TempRate;
import com.ondoset.domain.Enum.Weather;
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

	Boolean existsByIdAndMember(Long id, Member member);

	List<OOTD> findTop3ByMember_IdInOrderByIdDesc(List<Long> memberIdList);

	@Query("select o from OOTD o where o.reportedCount>0 and o.isBlinded=false")
	List<OOTD> findByReportedCountGreaterThan();

	@Query("select new com.ondoset.dto.ootd.OotdDTO(o.id, trunc((o.departTime+32400)/86400)*86400-32400 date, o.lowestTemp, o.highestTemp, o.imageURL) " +
			"from OOTD o where o.member=:member order by o.id desc limit 10")
	List<OotdDTO> pageMyProfile(@Param("member") Member member);

	@Query("select new com.ondoset.dto.ootd.OotdDTO(o.id, trunc((o.departTime+32400)/86400)*86400-32400 date, o.lowestTemp, o.highestTemp, o.imageURL) " +
			"from OOTD o where o.member=:member and o.id<:lastPage order by o.id desc limit 10")
	List<OotdDTO> pageMyProfile(@Param("member") Member member, @Param("lastPage") Long lastPage);

	@Query("select new com.ondoset.dto.ootd.OotdDTO(o.id, trunc((o.departTime+32400)/86400)*86400-32400 date, o.lowestTemp, o.highestTemp, o.imageURL) " +
			"from OOTD o where o.member!=:member and o.reportedCount<5 and o.isBlinded=false and o.weather=:weather and o.tempRate=:tempRage order by o.id desc limit 10")
	List<OotdDTO> pageWeather(@Param("member") Member member, @Param("weather") Weather weather, @Param("tempRage") TempRate tempRate);

	@Query("select new com.ondoset.dto.ootd.OotdDTO(o.id, trunc((o.departTime+32400)/86400)*86400-32400 date, o.lowestTemp, o.highestTemp, o.imageURL) " +
			"from OOTD o where o.member!=:member and o.reportedCount<5 and o.isBlinded=false and o.weather=:weather and o.tempRate=:tempRage and o.id<:lastPage order by o.id desc limit 10")
	List<OotdDTO> pageWeather(@Param("member") Member member, @Param("weather") Weather weather, @Param("tempRage") TempRate tempRate, @Param("lastPage") Long lastPage);

	@Query("select new com.ondoset.dto.ootd.OotdDTO(o.id, trunc((o.departTime+32400)/86400)*86400-32400 date, o.lowestTemp, o.highestTemp, o.imageURL) " +
			"from OOTD o where o.member in (:memberList) and o.reportedCount<5 and o.isBlinded=false order by o.id desc limit 10")
	List<OotdDTO> pageLatest(@Param("memberList") List<Member> memberList);

	@Query("select new com.ondoset.dto.ootd.OotdDTO(o.id, trunc((o.departTime+32400)/86400)*86400-32400 date, o.lowestTemp, o.highestTemp, o.imageURL) " +
			"from OOTD o where o.member in (:memberList) and o.reportedCount<5 and o.isBlinded=false and o.id<:lastPage order by o.id desc limit 10")
	List<OotdDTO> pageLatest(@Param("memberList") List<Member> memberList, @Param("lastPage") Long lastPage);

	@Query("select new com.ondoset.dto.ootd.OotdDTO(o.id, trunc((o.departTime+32400)/86400)*86400-32400 date, o.lowestTemp, o.highestTemp, o.imageURL) " +
			"from OOTD o join o.likes l where l.member=:member and o.reportedCount<5 and o.isBlinded=false order by o.id desc limit 10")
	List<OotdDTO> pageLike(@Param("member") Member member);

	@Query("select new com.ondoset.dto.ootd.OotdDTO(o.id, trunc((o.departTime+32400)/86400)*86400-32400 date, o.lowestTemp, o.highestTemp, o.imageURL) " +
			"from OOTD o join o.likes l where l.member=:member and o.reportedCount<5 and o.isBlinded=false and o.id<:lastPage order by o.id desc limit 10")
	List<OotdDTO> pageLike(@Param("member") Member member, @Param("lastPage") Long lastPage);

	@Query("select new com.ondoset.dto.ootd.OotdDTO(o.id, trunc((o.departTime+32400)/86400)*86400-32400 date, o.lowestTemp, o.highestTemp, o.imageURL) " +
			"from OOTD o where o.member=:member and o.reportedCount<5 and o.isBlinded=false order by o.id desc limit 10")
	List<OotdDTO> pageProfile(@Param("member") Member member);

	@Query("select new com.ondoset.dto.ootd.OotdDTO(o.id, trunc((o.departTime+32400)/86400)*86400-32400 date, o.lowestTemp, o.highestTemp, o.imageURL) " +
			"from OOTD o where o.member=:member and o.reportedCount<5 and o.isBlinded=false and o.id<:lastPage order by o.id desc limit 10")
	List<OotdDTO> pageProfile(@Param("member") Member member, @Param("lastPage") Long lastPage);
}
