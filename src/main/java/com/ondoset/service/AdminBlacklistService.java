package com.ondoset.service;

import com.ondoset.domain.Member;
import com.ondoset.dto.admin.blacklist.GetRootDTO;
import com.ondoset.dto.admin.blacklist.PutRootDTO;
import com.ondoset.dto.admin.blacklist.ReporterDTO;
import com.ondoset.repository.MemberRepository;
import com.ondoset.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class AdminBlacklistService {

	private final MemberRepository memberRepository;
	private final ReportRepository reportRepository;

	public List<GetRootDTO> getRoot() {

		return memberRepository.findByBanPeriodGreaterThan();
	}

	public PutRootDTO.res putRoot(Long memberId, PutRootDTO.req req) {

		Member member = memberRepository.findById(memberId).get();

		member.setBanPeriod(req.getBanPeriod());

		return new PutRootDTO.res(memberRepository.save(member).getId());
	}

	public List<ReporterDTO> getReporter() {

		// report 테이블에 한 번 이상 등록된 사용자를 조회
		return reportRepository.findReporterList();
	}
}
