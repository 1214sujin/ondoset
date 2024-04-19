package com.ondoset.service;

import com.ondoset.common.Kma;
import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.Clothes;
import com.ondoset.domain.Consisting;
import com.ondoset.domain.Coordi;
import com.ondoset.domain.Member;
import com.ondoset.dto.coordi.RootDTO;
import com.ondoset.dto.kma.PastWDTO;
import com.ondoset.repository.ClothesRepository;
import com.ondoset.repository.ConsistingRepository;
import com.ondoset.repository.CoordiRepository;
import com.ondoset.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

	public RootDTO.res postRoot(RootDTO.req req) {

		RootDTO.res res = new RootDTO.res();

		Long departTime = req.getDepartTime();
		long date = ((departTime+32400)/86400)*86400-32400;

		// 이미 사용자가 해당 날짜에 코디 데이터를 가지고 있다면 오류 반환
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Member member = memberRepository.findByName(name);
		if (coordiRepository.findByConsistings_Clothes_MemberAndDate(member, date) != null) {
			throw new CustomException(ResponseCode.COM4090);
		}
		
		// 현재 시각이 대상 날짜 다음날의 11시가 지나지 않은 시점이라면 오류 반환
		// 대상 date와 24+11시간 이상 차이나야 함
		long now = Instant.now().getEpochSecond();
		if (now - date < 126000) {
			throw new CustomException(ResponseCode.COM4000, "아직 등록할 수 없는 날짜입니다.");
		}

		// req 분해(2)
		Double lat = req.getLat();
		Double lon = req.getLon();
		Long arrivalTime = req.getArrivalTime();
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

		res.setDate(date);
		return res;
	}
}
