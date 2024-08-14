package com.ondoset.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ondoset.common.Ai;
import com.ondoset.common.Kma;
import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.*;
import com.ondoset.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import com.ondoset.domain.Enum.TempRate;
import com.ondoset.domain.Enum.Weather;
import com.ondoset.dto.ootd.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
	private final ReportRepository reportRepository;
	private final Kma kma;
	private final Ai ai;
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
		res.setUsername(member.getName());
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

	public OotdPageDTO getLatest(Long lastPage) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// ai로부터 현재 사용자와 비슷한 사용자의 아이디 목록 획득
		List<Long> memberIdList = ai.getSimilarUser(member.getId());

		// ootd 목록 생성
		List<OotdDTO> ootdList;
		if (memberIdList.isEmpty()) {

			// 전체 사용자를 대상으로 최신순 획득
			if (lastPage.equals(-1L)) {
				ootdList = ootdRepository.pageLatest();
			} else {
				ootdList = ootdRepository.pageLatest(lastPage);
			}
		} else {

			// 현재 사용자와 비슷한 사용자의 목록 획득
			List<Member> similarUserList = memberIdList.stream().map(memberId ->
					memberRepository.findById(memberId).get()
			).toList();

			// similarUserList에 속한 사용자들의 ootd를 최신순으로 획득
			if (lastPage.equals(-1L)) {
				ootdList = ootdRepository.pageLatest(similarUserList);
			} else {
				ootdList = ootdRepository.pageLatest(similarUserList, lastPage);
			}
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

		LocalDate banPeriod = member.getBanPeriod();

		// 정지 종료 날짜를 오늘로부터의 일수로 변환
		Integer res = banPeriod.compareTo(LocalDate.now());

		return new BanPeriodDTO(res);
	}

	public WeatherPreviewDTO.res getWeatherPreview(WeatherPreviewDTO.req req) {

		// req 분해
		Double lat = req.getLat();
		Double lon = req.getLon();
		LocalDateTime departTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(req.getDepartTime()), ZoneId.of("Asia/Seoul"));
		LocalDateTime arrivalTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(req.getArrivalTime()), ZoneId.of("Asia/Seoul"));

		// 날씨를 조회하려면 등록하려는 시간이 오늘 11시가 지난 시점에서 과거 날짜여야 함
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
		if (!kma.canGetWthrData(now, arrivalTime)) throw new CustomException(ResponseCode.COM4000, "아직 등록할 수 없는 날짜입니다.");

		if (arrivalTime.isBefore(departTime)) {
			throw new CustomException(ResponseCode.COM4000, "등록하려는 날짜가 잘못되었습니다.");
		}

		return kma.getStn(lat, lon, now)
				.thenCompose(stn -> kma.getWthrData(departTime, arrivalTime, stn))
				.thenApply(kma::getWeatherPreviewFrom).join();
	}

	public RootDTO.res postRoot(RootDTO.req req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 정지된 사용자인지 확인
		LocalDate banPeriod = member.getBanPeriod();
		if (banPeriod.compareTo(LocalDate.now()) > 0) throw new CustomException(ResponseCode.COM4030);

		OOTD ootd = new OOTD();
		ootd.setMember(member);
		ootd.setRegion(req.getRegion());
		ootd.setDepartTime(req.getDepartTime());
		ootd.setArrivalTime(req.getArrivalTime());
		ootd.setWeather(Weather.valueOfLower(req.getWeather()));

		Integer lowestTemp = req.getLowestTemp();
		ootd.setLowestTemp(lowestTemp);
		Integer highestTemp = req.getHighestTemp();
		ootd.setHighestTemp(highestTemp);

		// 기온 범위 처리
		double tempAvg = ((double) lowestTemp + (double) highestTemp) / 2;
		if (tempAvg < 5) ootd.setTempRate(TempRate.TELSE);
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
		Type type = new TypeToken<List<String>>(){}.getType();
		List<String> reqWearingList = new Gson().fromJson(req.getWearingList(), type);
		List<Wearing> wearingList = new ArrayList<>();
		for (String wearing : reqWearingList) {

			Wearing newWearing = new Wearing();
			newWearing.setOOTD(ootd);
			newWearing.setName(wearing);
			wearingList.add(newWearing);
		}
		wearingRepository.saveAll(wearingList);

		// 응답 정의
		RootDTO.res res = new RootDTO.res();
		res.setOotdId(insertId);

		return res;
	}

	public ModifyPageDTO getModifyPage(Long ootdId) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		if (!ootdRepository.existsByIdAndMember(ootdId, member)) {
			throw new CustomException(ResponseCode.COM4010, "요청된 자원에 접근할 수 없는 계정입니다: " + member.getName());
		}

		// 요청된 ootd 엔티티 획득
		OOTD ootd = ootdRepository.findById(ootdId).get();

		ModifyPageDTO res = new ModifyPageDTO();
		res.setOotdId(ootdId);
		res.setRegion(ootd.getRegion());
		res.setDepartTime(ootd.getDepartTime());
		res.setArrivalTime(ootd.getArrivalTime());
		res.setWeather(ootd.getWeather());
		res.setLowestTemp(ootd.getLowestTemp());
		res.setHighestTemp(ootd.getHighestTemp());
		res.setImageURL(ootd.getImageURL());

		// 입은 옷 정보
		List<String> wearingList = new ArrayList<>();
		for (Wearing wearing : ootd.getWearings()) {

			wearingList.add(wearing.getName());
		}

		res.setWearingList(wearingList);

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
		ootd.setRegion(req.getRegion());
		ootd.setDepartTime(req.getDepartTime());
		ootd.setArrivalTime(req.getArrivalTime());
		ootd.setWeather(Weather.valueOfLower(req.getWeather()));

		Integer lowestTemp = req.getLowestTemp();
		ootd.setLowestTemp(lowestTemp);
		Integer highestTemp = req.getHighestTemp();
		ootd.setHighestTemp(highestTemp);
		// 기온 범위 처리
		double tempAvg = ((double) lowestTemp + (double) highestTemp) / 2;
		if (tempAvg < 5) ootd.setTempRate(TempRate.TELSE);
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
		Type type = new TypeToken<List<String>>(){}.getType();
		List<String> reqWearingList = new Gson().fromJson(req.getWearingList(), type);
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

		// 이미 팔로잉된 사용자면 오류 반환
		if (followingRepository.existsByFollowerAndFollowed(member, followedMember)) {
			throw new CustomException(ResponseCode.COM4090);
		}

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

		if (following == null) {
			throw new CustomException(ResponseCode.COM4091);
		}

		followingRepository.delete(following);

		FollowDTO res = new FollowDTO();
		res.setMemberId(memberId);

		return res;
	}

	public GetRootDTO.res getRoot(GetRootDTO.req req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// req 분해
		Long ootdId = req.getOotdId();
		OOTD ootd = ootdRepository.findById(ootdId).get();
		Member viewedMember = ootd.getMember();

		// 아이디를 바탕으로 멤버 조회 및 ootd 개수 카운트, 팔로잉 여부 확인
		Long ootdCount = ootdRepository.countByMember(viewedMember);
		Boolean isFollowing = followingRepository.existsByFollowerAndFollowed(member, viewedMember);

		// 입은 옷 정보
		List<String> wearingList = new ArrayList<>();
		for (Wearing wearing : ootd.getWearings()) {

			wearingList.add(wearing.getName());
		}

		// 게시물 좋아요 여부 확인
		Boolean isLike = likeRepository.existsByMemberAndOotd(member, ootd);

		// 응답 생성
		GetRootDTO.res res = new GetRootDTO.res();
		res.setProfileShort(new ProfileShort(viewedMember.getId(), viewedMember.getNickname(), viewedMember.getProfileImage(), isFollowing, ootdCount));
		res.setWeather(ootd.getWeather());
		res.setWearing(wearingList);
		res.setIsLike(isLike);

		return res;
	}

	public void postReport(ReportDTO req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 정지된 사용자인지 확인
		LocalDate banPeriod = member.getBanPeriod();
		if (banPeriod.compareTo(LocalDate.now()) > 0) throw new CustomException(ResponseCode.COM4030);

		// req 분해
		Long ootdId = req.getOotdId();
		OOTD ootd = ootdRepository.findById(ootdId).get();

		// 이미 신고한 적이 있다면 오류 반환
		if (reportRepository.existsByReporterAndOotd(member, ootd)) {
			throw new CustomException(ResponseCode.COM4090);
		}

		ootd.setReportedCount(ootd.getReportedCount() + 1);

		ootdRepository.save(ootd);

		Report report = new Report();
		report.setOotd(ootd);
		report.setReporter(member);
		report.setReason(req.getReason());

		reportRepository.save(report);
	}

	public LikeDTO postList(LikeDTO req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// req 분해
		Long ootdId = req.getOotdId();
		OOTD ootd = ootdRepository.findById(ootdId).get();

		// 이미 좋아요한 게시물인지 확인
		if (likeRepository.existsByMemberAndOotd(member, ootd)) {
			throw new CustomException(ResponseCode.COM4090);
		}

		Like like = new Like();
		like.setMember(member);
		like.setOotd(ootd);

		likeRepository.save(like);

		return req;
	}

	public LikeDTO putList(Long ootdId) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 요청된 ootd
		OOTD ootd = ootdRepository.findById(ootdId).get();

		Like like = likeRepository.findByMemberAndOotd(member, ootd);
		if (like == null) {
			throw new CustomException(ResponseCode.COM4091);
		}
		likeRepository.delete(like);

		LikeDTO res = new LikeDTO();
		res.setOotdId(ootdId);

		return res;
	}
}
