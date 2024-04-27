package com.ondoset.service;
import com.ondoset.common.Kma;
import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.OOTD;
import com.ondoset.domain.Wearing;
import com.ondoset.dto.kma.PastWDTO;
import com.ondoset.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import com.ondoset.domain.Enum.TempRate;
import com.ondoset.domain.Enum.Weather;
import com.ondoset.domain.Following;
import com.ondoset.domain.Member;
import com.ondoset.dto.ootd.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@Service
@Transactional
public class OOTDService {

	private final MemberRepository memberRepository;
	private final OOTDRepository ootdRepository;
	private final WearingRepository wearingRepository;
	private final LikeRepository likeRepository;
	private final FollowingRepository followingRepository;
	private final Kma kma;
	@Value("${com.ondoset.resources.path}")
	private String resourcesPath;

	public MyProfileDTO getMyProfile() {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 사용자의 ootd 10개 조회
		List<OotdDTO> ootdList = ootdRepository.pageMyProfile(member);
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
		res.setMemberId(member.getName());
		res.setNickname(member.getNickname());
		res.setProfileImage(member.getProfileImage());
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
		List<OotdDTO> ootdList = ootdRepository.pageMyProfile(member, req.getLastPage());
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

		List<OotdDTO> ootdList;
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

		List<OotdDTO> ootdList;
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

	public FollowingPageDTO getFollowList(FollowingSearchDTO req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// req 분해
		String search = req.getSearch();
		Long lastPage = req.getLastPage();

		// 현재 사용자가 팔로잉하고 있는 아이디 목록 및 팔로잉 튜플 pk 획득
		List<Following> followingList;
		if (search == null) {

			if (lastPage.equals(-1L)) {
				followingList = followingRepository.pageFollowing(member);
			} else {
				followingList = followingRepository.pageFollowing(member, lastPage);
			}
		} else {

			if (lastPage.equals(-1L)) {
				followingList = followingRepository.pageFollowingSearch(member, search);
			} else {
				followingList = followingRepository.pageFollowingSearch(member, search, lastPage);
			}
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

	public BanPeriodDTO getBanPeriod() {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		return new BanPeriodDTO(member.getBanPeriod());
	}

	public PastWDTO getWeatherPreview(WeatherPreviewDTO req) {

		// req 분해
		Double lat = req.getLat();
		Double lon = req.getLon();
		Long departTime = req.getDepartTime();
		Long arrivalTime = req.getArrivalTime();

		if (arrivalTime < departTime) {
			throw new CustomException(ResponseCode.COM4000, "등록하려는 날짜가 잘못되었습니다.");
		}

		return kma.getPastW(lat, lon, departTime, arrivalTime);
	}

	public RootDTO.res postRoot(RootDTO.req req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		OOTD ootd = new OOTD();
		ootd.setMember(member);
		ootd.setDepartTime(req.getDepartTime());
		ootd.setArrivalTime(req.getArrivalTime());
		ootd.setWeather(Weather.valueOfLower(req.getWeather()));

		Integer lowestTemp = req.getLowestTemp();
		ootd.setLowestTemp(lowestTemp);
		Integer highestTemp = req.getHighestTemp();
		ootd.setHighestTemp(highestTemp);

		// 기온 범위 처리
		double tempAvg = ((double) lowestTemp + (double) highestTemp) / 2;
		if (tempAvg < 5) ootd.setTempRate(TempRate.TElse);
		else if (tempAvg < 9) ootd.setTempRate(TempRate.T5);
		else if (tempAvg < 12) ootd.setTempRate(TempRate.T9);
		else if (tempAvg < 17) ootd.setTempRate(TempRate.T12);
		else if (tempAvg < 20) ootd.setTempRate(TempRate.T17);
		else if (tempAvg < 23) ootd.setTempRate(TempRate.T20);
		else if (tempAvg < 27) ootd.setTempRate(TempRate.T23);
		else ootd.setTempRate(TempRate.T28);

		// 이미지 처리
		MultipartFile image = req.getImage();

		String filename = "/ootd/"+ UUID.randomUUID() + "_" + image.getOriginalFilename();
		Path savePath = Paths.get(resourcesPath+filename);

		try {
			image.transferTo(savePath);
			ootd.setImageURL(filename);
		}
		catch (Exception e) {
			throw new CustomException(ResponseCode.COM4150);
		}

		// ootd 생성
		Long insertId = ootdRepository.save(ootd).getId();

		// wearing list 처리
		List<String> reqWearingList = req.getWearingList();
		List<Wearing> wearingsList = new ArrayList<>();
		for (String wearing : reqWearingList) {

			Wearing newWearing = new Wearing();
			newWearing.setOOTD(ootd);
			newWearing.setName(wearing);
			wearingsList.add(newWearing);
		}
		wearingRepository.saveAll(wearingsList);

		// 응답 정의
		RootDTO.res res = new RootDTO.res();
		res.setOotdId(insertId);

		return res;
	}

	public RootDTO.res putRoot(Long ootdId, RootDTO.putReq req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		if (!ootdRepository.existsByIdAndMember(ootdId, member)) {
			throw new CustomException(ResponseCode.COM4010, "요청된 자원에 접근할 수 없는 계정입니다: " + member.getName());
		}

		// 요청된 ootd 엔티티 획득
		OOTD ootd = ootdRepository.findById(ootdId).get();

		// ootd 엔티티 수정
		ootd.setMember(member);
		ootd.setDepartTime(req.getDepartTime());
		ootd.setArrivalTime(req.getArrivalTime());
		ootd.setWeather(Weather.valueOfLower(req.getWeather()));

		Integer lowestTemp = req.getLowestTemp();
		ootd.setLowestTemp(lowestTemp);
		Integer highestTemp = req.getHighestTemp();
		ootd.setHighestTemp(highestTemp);
		// 기온 범위 처리
		double tempAvg = ((double) lowestTemp + (double) highestTemp) / 2;
		if (tempAvg < 5) ootd.setTempRate(TempRate.TElse);
		else if (tempAvg < 9) ootd.setTempRate(TempRate.T5);
		else if (tempAvg < 12) ootd.setTempRate(TempRate.T9);
		else if (tempAvg < 17) ootd.setTempRate(TempRate.T12);
		else if (tempAvg < 20) ootd.setTempRate(TempRate.T17);
		else if (tempAvg < 23) ootd.setTempRate(TempRate.T20);
		else if (tempAvg < 27) ootd.setTempRate(TempRate.T23);
		else ootd.setTempRate(TempRate.T28);

		// 이미지 처리
		MultipartFile image = req.getImage();

		if (image != null && !image.isEmpty()) {

			// 기존에 존재하던 이미지 파일은 삭제
			String existingImage = ootd.getImageURL();
			if (existingImage != null) new File(resourcesPath+existingImage).delete();

			String filename = "/ootd/" + UUID.randomUUID() + "_" + image.getOriginalFilename();
			Path savePath = Paths.get(resourcesPath+filename);

			try {
				image.transferTo(savePath);
				ootd.setImageURL(filename);
			}
			catch (Exception e) {
				throw new CustomException(ResponseCode.COM4150);
			}
		}

		// 기존 wearing list 삭제
		List<Wearing> existingWearingList = wearingRepository.findByOotd(ootd);
		for (Wearing wearing : existingWearingList) {
			wearingRepository.delete(wearing);
		}
		// wearing list 처리
		List<String> reqWearingList = req.getWearingList();
		List<Wearing> wearingList = new ArrayList<>();
		for (String wearing : reqWearingList) {

			Wearing newWearing = new Wearing();
			newWearing.setOOTD(ootd);
			newWearing.setName(wearing);
			wearingList.add(newWearing);
		}
		wearingRepository.saveAll(wearingList);

		ootdRepository.save(ootd);

		// 응답 정의
		RootDTO.res res = new RootDTO.res();
		res.setOotdId(ootd.getId());

		return res;
	}

	public void deleteRoot(Long ootdId) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		if (!ootdRepository.existsByIdAndMember(ootdId, member)) {
			throw new CustomException(ResponseCode.COM4010, "요청된 자원에 접근할 수 없는 계정입니다: " + member.getName());
		}

		// 요청된 ootd 엔티티 획득
		OOTD ootd = ootdRepository.findById(ootdId).get();

		// 이미지 파일 삭제
		String existingImage = ootd.getImageURL();
		if (existingImage != null) new File(resourcesPath+existingImage).delete();

		// 연관된 wearing list 삭제
		List<Wearing> existingWearingList = wearingRepository.findByOotd(ootd);
		for (Wearing wearing : existingWearingList) {
			wearingRepository.delete(wearing);
		}

		ootdRepository.delete(ootd);
	}

	public ProfileDTO.res getProfile(ProfileDTO.req req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 프로필이 요청된 사용자가 현재 사용자와 동일한지 확인
		Member viewedMember = memberRepository.findById(req.getMemberId()).get();

		if (viewedMember.equals(member)) {
			throw new CustomException(ResponseCode.COM4000, "현재 사용자의 프로필을 해당 경로로 조회할 수 없습니다.");
		}

		// 사용자의 ootd 10개 조회
		Long reqLastPage = req.getLastPage();

		List<OotdDTO> ootdList;
		if (reqLastPage == -1) {
			ootdList = ootdRepository.pageProfile(viewedMember);
		} else {
			ootdList = ootdRepository.pageProfile(viewedMember, reqLastPage);
		}

		Long lastPage;
		if (ootdList.size() < 10) {
			lastPage = -2L;
		} else {
			lastPage = ootdList.get(9).getOotdId();
		}

		// 응답 정의
		ProfileDTO.res res = new ProfileDTO.res();
		res.setLastPage(lastPage);
		res.setOotdList(ootdList);

		return res;
	}

	public FollowDTO postFollow(FollowDTO req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 팔로잉이 요청된 사용자
		Member followedMember = memberRepository.findById(req.getMemberId()).get();

		Following following = new Following();
		following.setFollower(member);
		following.setFollowed(followedMember);

		followingRepository.save(following);

		return req;
	}

	public FollowDTO putFollow(Long memberId) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 팔로잉 취소가 요청된 사용자
		Member followedMember = memberRepository.findById(memberId).get();

		Following following = followingRepository.findByFollowerAndFollowed(member, followedMember);

		followingRepository.delete(following);

		FollowDTO res = new FollowDTO();
		res.setMemberId(memberId);

		return res;
	}
}
