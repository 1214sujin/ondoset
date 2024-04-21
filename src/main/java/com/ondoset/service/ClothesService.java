package com.ondoset.service;

import com.ondoset.common.Kma;
import com.ondoset.dto.clothes.HomeDTO;
import com.ondoset.repository.ClothesRepository;
import com.ondoset.repository.ConsistingRepository;
import com.ondoset.repository.CoordiRepository;
import com.ondoset.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@RequiredArgsConstructor
@Service
@Transactional
public class ClothesService {

	private final MemberRepository memberRepository;
	private final CoordiRepository coordiRepository;
	private final ConsistingRepository consistingRepository;
	private final ClothesRepository clothesRepository;
	private final Kma kma;

	public HomeDTO.res getHome(HomeDTO.req req) {

		// req 분해
		Long date = req.getDate();
		Double lat = req.getLat();
		Double lon = req.getLon();

		// HomeDTO.res.plan 획득

		// HomeDTO.res.record 획득

		// HomeDTO.res.recommend 획득

		// HomeDTO.res.ootd 획득

		// 응답 정의
		HomeDTO.res res = new HomeDTO.res();
		res.setForecast(kma.getForecast(lat, lon, date));
//		res.setPlan();
//		res.setRecord();
//		res.setRecommend();
//		res.setOotd();

		return res;
	}
}
