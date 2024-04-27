package com.ondoset.service;

import com.ondoset.domain.Member;
import com.ondoset.dto.admin.blacklist.GetRootDTO;
import com.ondoset.dto.admin.blacklist.PutRootDTO;
import com.ondoset.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class AdminService {

	private final MemberRepository memberRepository;

	public List<GetRootDTO> getRoot() {

		return memberRepository.findByBanPeriodGreaterThan();
	}

	public PutRootDTO.res putRoot(Long memberId, PutRootDTO.req req) {

		Member member = memberRepository.findById(memberId).get();

		member.setBanPeriod(req.getBanPeriod());

		return new PutRootDTO.res(memberRepository.save(member).getId());
	}
}
