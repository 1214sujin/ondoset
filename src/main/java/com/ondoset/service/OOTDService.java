package com.ondoset.service;

import com.ondoset.domain.Member;
import com.ondoset.dto.OOTD.MyProfileDTO;
import com.ondoset.dto.OOTD.OotdDTO;
import com.ondoset.repository.FollowingRepository;
import com.ondoset.repository.LikeRepository;
import com.ondoset.repository.MemberRepository;
import com.ondoset.repository.OOTDRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.TimeZone;

@Log4j2
@RequiredArgsConstructor
@Service
@Transactional
public class OOTDService {

	private final MemberRepository memberRepository;
	private final OOTDRepository ootdRepository;
	private final LikeRepository likeRepository;
	private final FollowingRepository followingRepository;

	public MyProfileDTO getMyProfile() {

		// 현재 사용자 조회
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Member member = memberRepository.findByName(name);

		// 사용자의 ootd 10개 조회 / 총계
		List<OotdDTO> ootdList = ootdRepository.pageMyProfile(TimeZone.getDefault().getRawOffset(), member);
		Long lastPage;
		if (ootdList.size() < 10) {
			lastPage = -2L;
		} else {
			lastPage = ootdList.get(9).getOotdId();
		}

		// 작성한 ootd / 공감한 ootd / 팔로잉 총계
		Long ootdCount = ootdRepository.countByMember(member);
		Long likeCount = likeRepository.countByMember(member);
		Long followingCount = followingRepository.countByFollower(member);

		// 응답 정의
		MyProfileDTO res = new MyProfileDTO();
		res.setNickname(member.getNickname());
		res.setOotdCount(ootdCount);
		res.setLikeCount(likeCount);
		res.setFollowingCount(followingCount);
		res.setLastPage(lastPage);
		res.setOotdList(ootdList);

		return res;
	}
}
