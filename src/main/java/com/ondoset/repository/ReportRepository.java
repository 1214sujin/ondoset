package com.ondoset.repository;

import com.ondoset.domain.Member;
import com.ondoset.domain.OOTD;
import com.ondoset.domain.Report;
import com.ondoset.dto.admin.blacklist.ReporterDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

	Boolean existsByReporterAndOotd(Member member, OOTD ootd);

	@Query("select new com.ondoset.dto.admin.blacklist.ReporterDTO(m.id, m.nickname, count(r)) " +
			"from Report r left join r.reporter m group by r.reporter")
	List<ReporterDTO> findReporterList();
}
