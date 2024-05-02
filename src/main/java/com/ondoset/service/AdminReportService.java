package com.ondoset.service;

import com.ondoset.domain.OOTD;
import com.ondoset.dto.admin.report.ReportDTO;
import com.ondoset.repository.OOTDRepository;
import com.ondoset.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class AdminReportService {

	private final OOTDRepository ootdRepository;
	private final ReportRepository reportRepository;

	public List<ReportDTO> getRoot() {

		// 신고 횟수가 1 이상인 ootd를 조회
		List<OOTD> ootdList = ootdRepository.findByReportedCountGreaterThan();

		// 각 ootd의 신고 사유 목록 획득
		List<ReportDTO> res = new ArrayList<>();
		for (OOTD o : ootdList) {

			ReportDTO r = new ReportDTO();
			r.setOotdId(o.getId());
			r.setReportedCount(o.getReportedCount());
			r.setImageURL(o.getImageURL());
			r.setReason(reportRepository.findReasonByOotd(o));

			res.add(r);
		}

		return res;
	}

	public void putCount(Long ootdId) {

		OOTD ootd = ootdRepository.findById(ootdId).get();

		ootd.setReportedCount(0);

		ootdRepository.save(ootd);
	}
}
