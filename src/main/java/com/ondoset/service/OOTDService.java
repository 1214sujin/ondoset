package com.ondoset.service;

import com.ondoset.domain.Enum.TempRate;
import com.ondoset.domain.Enum.Weather;
import com.ondoset.domain.Following;
import com.ondoset.domain.Member;
import com.ondoset.dto.ootd.*;
import com.ondoset.repository.FollowingRepository;
import com.ondoset.repository.LikeRepository;
import com.ondoset.repository.MemberRepository;
import com.ondoset.repository.OOTDRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 사용자의 ootd 10개 조회
		List<Ootd> ootdList = ootdRepository.pageMyProfile(member);
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

	public MyProfilePageDTO getMyProfilePage(PageDTO req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 사용자의 ootd 10개 조회
		List<Ootd> ootdList = ootdRepository.pageMyProfile(member, req.getLastPage());
		Long lastPage;
		if (ootdList.size() < 10) {
			lastPage = -2L;
		} else {
			lastPage = ootdList.get(9).getOotdId();
		}

		// 응답 정의
		MyProfilePageDTO res = new MyProfilePageDTO();
		res.setLastPage(lastPage);
		res.setOotdList(ootdList);

		return res;
	}

	public OotdPageDTO getWeather(WeatherDTO req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// req 분해
		Weather weather = Weather.valueOfLower(req.getWeather());
		TempRate tempRate = TempRate.valueOfLower(req.getTempRate());
		Long lastPage = req.getLastPage();

		List<Ootd> ootdList;
		if (lastPage.equals(-1L)) {
			ootdList = ootdRepository.pageWeather(member, weather, tempRate);
		} else {
			ootdList = ootdRepository.pageWeather(member, weather, tempRate, lastPage);
		}

		OotdPageDTO res = new OotdPageDTO();

		res.setOotdList(ootdList);
		if (ootdList.size() < 10) {
			res.setLastPage(-2L);
		} else {
			res.setLastPage(ootdList.get(9).getOotdId());
		}

		return res;
	}

	public OotdPageDTO getLikeList(PageDTO req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// req 분해
		Long lastPage = req.getLastPage();

		List<Ootd> ootdList;
		if (lastPage.equals(-1L)) {
			ootdList = ootdRepository.pageLike(member);
		} else {
			ootdList = ootdRepository.pageLike(member, lastPage);
		}

		OotdPageDTO res = new OotdPageDTO();

		res.setOotdList(ootdList);
		if (ootdList.size() < 10) {
			res.setLastPage(-2L);
		} else {
			res.setLastPage(ootdList.get(9).getOotdId());
		}

		return res;
	}

	public FollowingPageDTO getFollowList(PageDTO req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// req 분해
		Long lastPage = req.getLastPage();

		// 현재 사용자가 팔로잉하고 있는 아이디 목록 및 팔로잉 튜플 pk 획득
		List<Following> followingList;
		if (lastPage.equals(-1L)) {
			followingList = followingRepository.pageFollowing(member);
		} else {
			followingList = followingRepository.pageFollowing(member, lastPage);
		}

		// 아이디를 바탕으로 멤버 조회 및 ootd 개수 카운트
		List<ProfileShort> followingMemberList = new ArrayList<>();
		for (Following f : followingList) {
			Member m = f.getFollowed();
			Long c = ootdRepository.countByMember(m);

			followingMemberList.add(new ProfileShort(m.getId(), m.getNickname(), m.getProfileImage(), true, c));
		}

		FollowingPageDTO res = new FollowingPageDTO();

		res.setFollowingList(followingMemberList);
		if (followingList.size() < 24) {
			res.setLastPage(-2L);
		} else {
			res.setLastPage(followingList.get(23).getId());
		}

		return res;
	}
}
