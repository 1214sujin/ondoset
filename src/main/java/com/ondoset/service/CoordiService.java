package com.ondoset.service;

import com.ondoset.common.Ai;
import com.ondoset.common.Kma;
import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.Clothes;
import com.ondoset.domain.Consisting;
import com.ondoset.domain.Coordi;
import com.ondoset.domain.Enum.Satisfaction;
import com.ondoset.domain.Member;
import com.ondoset.dto.clothes.ClothesDTO;
import com.ondoset.dto.coordi.*;
import com.ondoset.dto.kma.PastWDTO;
import com.ondoset.repository.ClothesRepository;
import com.ondoset.repository.ConsistingRepository;
import com.ondoset.repository.CoordiRepository;
import com.ondoset.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@Service
@Transactional
public class CoordiService {

	private final MemberRepository memberRepository;
	private final CoordiRepository coordiRepository;
	private final ConsistingRepository consistingRepository;
	private final ClothesRepository clothesRepository;
	private final Kma kma;
	private final Ai ai;
	@Value("${com.ondoset.resources.path}")
	private String resourcesPath;

	public SatisfactionPredDTO.res postSatisfactionPred(List<FullTagDTO> tagComb) {

		Satisfaction satisfaction = ai.getSatisfaction(tagComb);

		SatisfactionPredDTO.res res = new SatisfactionPredDTO.res();
		res.setPred(satisfaction);

		return res;
	}

	public DateDTO postRoot(PostRootDTO req) {

		Long departTime = req.getDepartTime();
		long date = ((departTime+32400)/86400)*86400-32400;

		// 이미 사용자가 해당 날짜에 코디 데이터를 가지고 있다면 오류 반환
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());
		if (coordiRepository.existsByConsistings_Clothes_MemberAndDate(member, date)) {
			throw new CustomException(ResponseCode.COM4090);
		}

		// 현재 시각이 대상 날짜가 지나지 않은 시점이라면 오류 반환
		// 들어온 시간을 기준으로 오늘 날짜와 24시간 이상 차이나야 함
		Long arrivalTime = req.getArrivalTime();
		long now = Instant.now().getEpochSecond();
		if ((now - ((arrivalTime+32400)/86400)*86400-32400) < 86400) {
			throw new CustomException(ResponseCode.COM4000, "아직 등록할 수 없는 날짜입니다.");
		}
		if (arrivalTime < departTime) {
			throw new CustomException(ResponseCode.COM4000, "등록하려는 날짜가 잘못되었습니다.");
		}

		// req 분해
		Double lat = req.getLat();
		Double lon = req.getLon();
		List<Long> clothesIdList = req.getClothesList();

		// coordi, consisting에 들어갈 친구들 생성
		PastWDTO pastW = kma.getPastW(lat, lon, departTime, arrivalTime);
		List<Clothes> clothesList = clothesRepository.findByIdIn(clothesIdList);

		// coordi 정의
		Coordi coordi = new Coordi();
		coordi.setDate(date);
		coordi.setDepartTime(departTime);
		coordi.setArrivalTime(arrivalTime);
		coordi.setWeather(pastW.getWeather());
		coordi.setLowestTemp(pastW.getLowestTemp());
		coordi.setHighestTemp(pastW.getHighestTemp());

		coordiRepository.save(coordi);

		// consisting 정의
		ArrayList<Consisting> consistingList = new ArrayList<>();
		for (Clothes ct : clothesList) {

			Consisting consisting = new Consisting();
			consisting.setConsistings(coordi, ct);

			consistingList.add(consisting);
		}
		consistingRepository.saveAll(consistingList);

		DateDTO res = new DateDTO();

		res.setDate(date);
		return res;
	}

	public DateDTO postPlan(PlanDTO req) {

		Long date = req.getDate();

		// 이미 사용자가 해당 날짜에 코디 데이터를 가지고 있다면 오류 반환
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());
		if (coordiRepository.existsByConsistings_Clothes_MemberAndDate(member, date)) {
			throw new CustomException(ResponseCode.COM4090);
		}

		// coordi 정의
		Coordi coordi = new Coordi();
		coordi.setDate(date);
		coordiRepository.save(coordi);

		// consisting 정의
		List<Long> clothesIdList = req.getClothesList();
		List<Clothes> clothesList = clothesRepository.findByIdIn(clothesIdList);

		ArrayList<Consisting> consistingList = new ArrayList<>();
		for (Clothes ct : clothesList) {

			Consisting consisting = new Consisting();
			consisting.setConsistings(coordi, ct);

			consistingList.add(consisting);
		}
		consistingRepository.saveAll(consistingList);

		DateDTO res = new DateDTO();

		res.setDate(date);
		return res;
	}

	public List<GetRootDTO.res> getRoot(int year, int month) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 날짜가 해당하는 달의 coordi 아이디 리스트 획득
		List<Long> coordiList = coordiRepository.findByMemberAndMonth(member, year, String.format("%02d", month));

		// 각 coordi를 돌면서 clothesList 획득
		List<GetRootDTO.res> resList = new ArrayList<>();
		for (Long coordi_id : coordiList) {

			Coordi coordi = coordiRepository.findById(coordi_id).get();

			GetRootDTO.res res = new GetRootDTO.res();
			res.setCoordiId(coordi.getId());

			LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(coordi.getDate()), ZoneId.of("Asia/Seoul"));
			res.setYear(dateTime.getYear());
			res.setMonth(dateTime.getMonthValue());
			res.setDay(dateTime.getDayOfMonth());

			res.setSatisfaction(coordi.getSatisfaction());
			res.setDepartTime(coordi.getDepartTime());
			res.setArrivalTime(coordi.getArrivalTime());
			res.setWeather(coordi.getWeather());
			res.setLowestTemp(coordi.getLowestTemp());
			res.setHighestTemp(coordi.getHighestTemp());
			res.setImageURL(coordi.getImageURL());

			List<ClothesDTO> clothesList = new ArrayList<>();
			for (Consisting consisting : coordi.getConsistings()) {

				Clothes clothes = consisting.getClothes();

				ClothesDTO clothesDTO = new ClothesDTO();
				clothesDTO.setClothesId(clothes.getId());
				clothesDTO.setName(clothes.getName());
				clothesDTO.setImageURL(clothes.getImageURL());
				clothesDTO.setCategory(clothes.getTag().getCategory());
				clothesDTO.setTag(clothesDTO.getTag());
				clothesDTO.setThickness(clothes.getThickness());

				clothesList.add(clothesDTO);
			}
			res.setClothesList(clothesList);

			resList.add(res);
		}

		return resList;
	}

	public DateDTO putImage(Long coordiId, ImageDTO req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		if (!coordiRepository.existsByIdAndConsistings_Clothes_Member(coordiId, member)) {
			throw new CustomException(ResponseCode.COM4010, "요청된 자원에 접근할 수 없는 계정입니다: " + member.getName());
		}

		// 요청된 coordi 엔티티 획득
		Coordi coordi = coordiRepository.findById(coordiId).get();

		MultipartFile pic = req.getImage();

		// 기존에 존재하던 이미지 파일이 있다면 삭제
		String existingImage = coordi.getImageURL();
		if (existingImage != null) new File(resourcesPath+existingImage).delete();

		if (pic == null || pic.isEmpty()) {

			coordi.setImageURL(null);
		}
		else {

			String filename = "/coordi/"+ UUID.randomUUID() +"_"+pic.getOriginalFilename();
			Path savePath = Paths.get(resourcesPath+filename);

			try {
				pic.transferTo(savePath);
				coordi.setImageURL(filename);
			}
			catch (Exception e) {
				throw new CustomException(ResponseCode.COM4150);
			}
		}
		DateDTO res = new DateDTO();
		res.setDate(coordiRepository.save(coordi).getDate());

		return res;
	}

	public DateDTO putRoot(Long coordiId, PutRootDTO req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		if (!coordiRepository.existsByIdAndConsistings_Clothes_Member(coordiId, member)) {
			throw new CustomException(ResponseCode.COM4010, "요청된 자원에 접근할 수 없는 계정입니다: " + member.getName());
		}

		// 요청된 coordi 엔티티 획득
		Coordi coordi = coordiRepository.findById(coordiId).get();

		// req 분해
		List<Long> clothesIdList = req.getClothesList();

		// coordi의 기존 consistings 삭제
		consistingRepository.deleteAll(coordi.getConsistings());

		// coordi의 consistings 재정의
		List<Clothes> clothesList = clothesRepository.findByIdIn(clothesIdList);
		coordiRepository.save(coordi);

		// consisting 정의
		ArrayList<Consisting> consistingList = new ArrayList<>();
		for (Clothes ct : clothesList) {

			Consisting consisting = new Consisting();
			consisting.setConsistings(coordi, ct);

			consistingList.add(consisting);
		}
		consistingRepository.saveAll(consistingList);

		DateDTO res = new DateDTO();

		res.setDate(coordi.getDate());
		return res;
	}

	public void deleteRoot(Long coordiId) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		if (!coordiRepository.existsByIdAndConsistings_Clothes_Member(coordiId, member)) {
			throw new CustomException(ResponseCode.COM4010, "요청된 자원에 접근할 수 없는 계정입니다: " + member.getName());
		}

		// 요청된 coordi 엔티티 획득
		Coordi coordi = coordiRepository.findById(coordiId).get();

		// 기존에 존재하던 이미지 파일이 있다면 삭제
		String existingImage = coordi.getImageURL();
		if (existingImage != null) new File(resourcesPath+existingImage).delete();

		// coordi의 기존 consistings 삭제
		consistingRepository.deleteAll(coordi.getConsistings());

		coordiRepository.delete(coordi);
	}

	public DateDTO putOutTime(Long coordiId, OutTimeDTO req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		if (!coordiRepository.existsByIdAndConsistings_Clothes_Member(coordiId, member)) {
			throw new CustomException(ResponseCode.COM4010, "요청된 자원에 접근할 수 없는 계정입니다: " + member.getName());
		}

		// 요청된 coordi 엔티티 획득
		Coordi coordi = coordiRepository.findById(coordiId).get();

		// 현재 시각이 대상 날짜가 지나지 않은 시점이라면 오류 반환
		// 들어온 시간을 기준으로 오늘 날짜와 24시간 이상 차이나야 함
		Long arrivalTime = req.getArrivalTime();
		long now = Instant.now().getEpochSecond();
		if ((now - ((arrivalTime+32400)/86400)*86400-32400) < 86400) {
			throw new CustomException(ResponseCode.COM4000, "아직 등록할 수 없는 날짜입니다.");
		}
		// departTime을 기준으로 한 날짜가 해당 coordi가 등록된 날짜와 다른 경우 오류 반환
		Long departTime = req.getDepartTime();
		long date = ((departTime+32400)/86400)*86400-32400;
		if (date != coordi.getDate() || arrivalTime < departTime) {
			throw new CustomException(ResponseCode.COM4000, "등록하려는 날짜가 잘못되었습니다.");
		}

		// req 분해
		Double lat = req.getLat();
		Double lon = req.getLon();

		// 날씨 계산
		PastWDTO pastW = kma.getPastW(lat, lon, departTime, arrivalTime);

		// coordi 수정
		coordi.setDepartTime(departTime);
		coordi.setArrivalTime(arrivalTime);
		coordi.setWeather(pastW.getWeather());
		coordi.setLowestTemp(pastW.getLowestTemp());
		coordi.setHighestTemp(pastW.getHighestTemp());

		coordiRepository.save(coordi);

		DateDTO res = new DateDTO();

		res.setDate(date);
		return res;
	}

	public DateDTO putSatisfaction(Long coordiId, SatisfactionDTO req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		if (!coordiRepository.existsByIdAndConsistings_Clothes_Member(coordiId, member)) {
			throw new CustomException(ResponseCode.COM4010, "요청된 자원에 접근할 수 없는 계정입니다: " + member.getName());
		}

		// 요청된 coordi 엔티티 획득
		Coordi coordi = coordiRepository.findById(coordiId).get();

		coordi.setSatisfaction(Satisfaction.valueOfLower(req.getSatisfaction()));

		DateDTO res = new DateDTO();

		res.setDate(coordi.getDate());
		return res;
	}
}
