package com.ondoset.repository;

import com.ondoset.domain.Member;
import com.ondoset.domain.OOTD;
import com.ondoset.domain.Report;
import com.ondoset.dto.admin.blacklist.ReporterDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

	Boolean existsByReporterAndOotd(Member member, OOTD ootd);

	List<Report> findByOotd(OOTD ootd);

	Report findByReporterAndOotd(Member member, OOTD ootd);

	List<Report> findByOotdAndIsProcessed(OOTD ootd, Boolean isProcessed);
	default List<Report> findByOotdAndIsProcessedIsFalse(OOTD ootd) {
		return findByOotdAndIsProcessed(ootd, false);
	}

	@Query("select r.reason from Report r where r.ootd=:ootd and r.isProcessed=false")
	List<String> findReasonByOotd(@Param("ootd") OOTD ootd);

	@Query("select new com.ondoset.dto.admin.blacklist.ReporterDTO(m.id, m.nickname, count(r)) " +
			"from Report r join r.reporter m group by r.reporter")
	List<ReporterDTO> findReporterList();

	@Query("select distinct o " +
			"from Report r join r.ootd o where r.reporter=:member order by o.id desc limit 10")
	List<OOTD> findReportingOotdList(@Param("member") Member member);

	@Query("select distinct o " +
			"from Report r join r.ootd o where r.reporter=:member and o.id<:lastPage order by o.id desc limit 10")
	List<OOTD> findReportingOotdList(@Param("member") Member member, @Param("lastPage") Long lastPage);

	@Query("select new com.ondoset.dto.admin.blacklist.ReporterDTO(m.id, m.nickname, count(r)) " +
			"from Report r join r.ootd o join o.member m group by o.member")
	List<ReporterDTO> findReportedList();

	@Query("select distinct o " +
			"from Report r join r.ootd o where o.member=:member order by o.id desc limit 10")
	List<OOTD> findReportedOotdList(@Param("member") Member member);

	@Query("select distinct o " +
			"from Report r join r.ootd o where o.member=:member and o.id<:lastPage order by o.id desc limit 10")
	List<OOTD> findReportedOotdList(@Param("member") Member member, @Param("lastPage") Long lastPage);
}
