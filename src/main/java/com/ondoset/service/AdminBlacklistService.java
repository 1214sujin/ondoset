package com.ondoset.service;

import com.ondoset.domain.Member;
import com.ondoset.domain.OOTD;
import com.ondoset.domain.Report;
import com.ondoset.domain.Wearing;
import com.ondoset.dto.admin.blacklist.*;
import com.ondoset.repository.MemberRepository;
import com.ondoset.repository.OOTDRepository;
import com.ondoset.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class AdminBlacklistService {

	private final MemberRepository memberRepository;
	private final ReportRepository reportRepository;
	private final OOTDRepository ootdRepository;

	public List<GetRootDTO> getRoot() {

		return memberRepository.findByBanPeriodGreaterThan();
	}

	public PutRootDTO.res putRoot(Long memberId, PutRootDTO.req req) {

		Member member = memberRepository.findById(memberId).get();

		LocalDate banPeriod = LocalDate.now().plusDays(req.getBanPeriod());
		member.setBanPeriod(banPeriod);

		return new PutRootDTO.res(memberRepository.save(member).getId());
	}

	public List<ReporterDTO> getReporter() {

		// report 테이블에 한 번 이상 등록된 사용자를 조회
		return reportRepository.findReporterList();
	}

	public ReporterListDTO.res getReporterList(ReporterListDTO.req req) {

		// req 분해
		Member member = memberRepository.findById(req.getMemberId()).get();
		Long reqLastPage = req.getLastPage();

		List<Long> reportedOotdIdList;
		if (reqLastPage == -1) {
			reportedOotdIdList = reportRepository.findReportingOotdList(member);
		} else {
			reportedOotdIdList = reportRepository.findReportingOotdList(member, reqLastPage);
		}

		// wearing, reason 정보 전달
		List<ReportedOotdDTO> reportedOotdList = new ArrayList<>();
		for (Long reportingOotdId : reportedOotdIdList) {

			OOTD ootd = ootdRepository.findById(reportingOotdId).get();
			ReportedOotdDTO reportedOotd = new ReportedOotdDTO();
			reportedOotd.setOotdId(reportingOotdId);
			reportedOotd.setDate(((ootd.getDepartTime()+32400)/86400)*86400-32400);
			reportedOotd.setWeather(ootd.getWeather());
			reportedOotd.setLowestTemp(ootd.getLowestTemp());
			reportedOotd.setHighestTemp(ootd.getHighestTemp());
			reportedOotd.setImageURL(ootd.getImageURL());

			// 입은 옷 정보
			List<String> wearingList = new ArrayList<>();
			for (Wearing wearing : ootd.getWearings()) {

				wearingList.add(wearing.getName());
			}
			reportedOotd.setWearing(wearingList);

			// 신고 사유
			List<String> reasonList = new ArrayList<>();
			for (Report r : reportRepository.findByOotd(ootd)) {

				reasonList.add(r.getReason());
			}
			reportedOotd.setReason(reasonList);

			reportedOotdList.add(reportedOotd);
		}

		Long lastPage;
		if (reportedOotdList.size() < 10) {
			lastPage = -2L;
		} else {
			lastPage = reportedOotdList.get(9).getOotdId();
		}

		ReporterListDTO.res res = new ReporterListDTO.res();
		res.setLastPage(lastPage);
		res.setOotdList(reportedOotdList);

		return res;
	}

	public List<ReporterDTO> getReported() {

		// report 테이블에 한 번 이상 등록된 ootd를 작성한 사용자를 조회
		return reportRepository.findReportedList();
	}

	public ReporterListDTO.res getReportedList(ReporterListDTO.req req) {

		// req 분해
		Member member = memberRepository.findById(req.getMemberId()).get();
		Long reqLastPage = req.getLastPage();

		List<Long> reportedOotdIdList;
		if (reqLastPage == -1) {
			reportedOotdIdList = reportRepository.findReportedOotdList(member);
		} else {
			reportedOotdIdList = reportRepository.findReportedOotdList(member, reqLastPage);
		}

		// wearing, reason 정보 전달
		List<ReportedOotdDTO> reportedOotdList = new ArrayList<>();
		for (Long reportingOotdId : reportedOotdIdList) {

			OOTD ootd = ootdRepository.findById(reportingOotdId).get();
			ReportedOotdDTO reportedOotd = new ReportedOotdDTO();
			reportedOotd.setOotdId(reportingOotdId);
			reportedOotd.setDate(((ootd.getDepartTime()+32400)/86400)*86400-32400);
			reportedOotd.setWeather(ootd.getWeather());
			reportedOotd.setLowestTemp(ootd.getLowestTemp());
			reportedOotd.setHighestTemp(ootd.getHighestTemp());
			reportedOotd.setImageURL(ootd.getImageURL());

			// 입은 옷 정보
			List<String> wearingList = new ArrayList<>();
			for (Wearing wearing : ootd.getWearings()) {

				wearingList.add(wearing.getName());
			}
			reportedOotd.setWearing(wearingList);

			// 신고 사유
			List<String> reasonList = new ArrayList<>();
			for (Report r : reportRepository.findByOotd(ootd)) {

				reasonList.add(r.getReason());
			}
			reportedOotd.setReason(reasonList);

			reportedOotdList.add(reportedOotd);
		}

		Long lastPage;
		if (reportedOotdList.size() < 10) {
			lastPage = -2L;
		} else {
			lastPage = reportedOotdList.get(9).getOotdId();
		}

		ReporterListDTO.res res = new ReporterListDTO.res();
		res.setLastPage(lastPage);
		res.setOotdList(reportedOotdList);

		return res;
	}
}
