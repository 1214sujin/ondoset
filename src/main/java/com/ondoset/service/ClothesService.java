package com.ondoset.service;
import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.Clothes;
import com.ondoset.domain.Enum.Category;
import com.ondoset.domain.Enum.Thickness;
import com.ondoset.dto.clothes.*;
import com.ondoset.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import com.ondoset.common.Kma;
import com.ondoset.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@Service
@Transactional
public class ClothesService {

	private final MemberRepository memberRepository;
	private final ClothesRepository clothesRepository;
	private final TagRepository tagRepository;
	private final Kma kma;
	@Value("${com.ondoset.resources.path}")
	private String resourcesPath;

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

	public AllDTO.res getAll(AllDTO.req req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 페이지 확인
		long reqLastPage = req.getLastPage();

		// 사용자의 clothes 18개 조회
		List<ClothesDTO> clothesList;
		String reqCategory = req.getCategory();
		if (reqCategory != null) {

			// 카테고리가 요청된 경우
			Category category;
			try {
				category = Category.valueOfLower(reqCategory);
			} catch (Exception e) {
				throw new CustomException(ResponseCode.COM4000);
			}

			if (reqLastPage == -1) {
				clothesList = clothesRepository.pageAllClothes(member, category);
			} else {
				clothesList = clothesRepository.pageAllClothes(member, category, reqLastPage);
			}
		} else {

			// 전체 요청된 경우
			if (reqLastPage == -1) {
				clothesList = clothesRepository.pageAllClothes(member);
			} else {
				clothesList = clothesRepository.pageAllClothes(member, reqLastPage);
			}
		}

		// 마지막에 조회된 clothes id
		Long lastPage;
		if (clothesList.size() < 18) {
			lastPage = -2L;
		} else {
			lastPage = clothesList.get(17).getClothesId();
		}

		// 응답 정의
		AllDTO.res res = new AllDTO.res();
		res.setLastPage(lastPage);
		res.setClothesList(clothesList);

		Thickness.class.getEnumConstants();

		return res;
	}

	public TagDTO getTag() {

		TagDTO res = new TagDTO();
		res.setTop(tagRepository.findTop());
		res.setBottom(tagRepository.findBottom());
		res.setOuter(tagRepository.findOuter());
		res.setShoe(tagRepository.findShoe());
		res.setAcc(tagRepository.findAcc());

		return res;
	}

	public RootDTO.res postRoot(RootDTO.req req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// clothes 엔티티 생성
		Clothes clothes = new Clothes();
		clothes.setMember(member);
		clothes.setTag(tagRepository.findById(req.getTagId()).get());
		clothes.setName(req.getName());
		clothes.setThickness(Thickness.valueOfLower(req.getThickness()));

		// 이미지 처리
		MultipartFile image = req.getImage();

		if (image == null || image.isEmpty()) {

			clothes.setImageURL(null);
		}
		else {

			String filename = "/clothes/" + UUID.randomUUID() + "_" + image.getOriginalFilename();
			Path savePath = Paths.get(resourcesPath+filename);

			try {
				image.transferTo(savePath);
				clothes.setImageURL(filename);
			}
			catch (Exception e) {
				throw new CustomException(ResponseCode.COM4150);
			}
		}

		// 응답 정의
		RootDTO.res res = new RootDTO.res();
		res.setClothesId(clothesRepository.save(clothes).getId());

		return res;
	}

	public RootDTO.res putRoot(Long clothesId, RootDTO.putReq req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		if (!clothesRepository.existsByIdAndMember(clothesId, member)) {
			throw new CustomException(ResponseCode.COM4010, "요청된 자원에 접근할 수 없는 계정입니다: " + member.getName());
		}

		// 요청된 옷 엔티티 획득
		Clothes clothes = clothesRepository.findById(clothesId).get();
		if (clothes.getIsDeleted()) throw new CustomException(ResponseCode.COM4091);

		// clothes 엔티티 수정
		clothes.setMember(member);
		clothes.setTag(tagRepository.findById(req.getTagId()).get());
		clothes.setName(req.getName());
		clothes.setThickness(Thickness.valueOfLower(req.getThickness()));

		// 이미지 처리
		Boolean imageUpdated = req.getImageUpdated();
		// imageUpdated가 true인 경우, req.image를 수정
		if (imageUpdated) {

			MultipartFile image = req.getImage();

			// 기존에 존재하던 이미지 파일은 삭제(확장자가 다를 수도 있으니 보내주는 파일명을 항상 그대로 사용)
			String existingImage = clothes.getImageURL();
			if (existingImage != null) new File(resourcesPath+existingImage).delete();

			if (image == null || image.isEmpty()) {

				clothes.setImageURL(null);
			}
			else {

				String filename = "/clothes/" + UUID.randomUUID() + "_" + image.getOriginalFilename();
				Path savePath = Paths.get(resourcesPath+filename);

				try {
					image.transferTo(savePath);
					clothes.setImageURL(filename);
				}
				catch (Exception e) {
					throw new CustomException(ResponseCode.COM4150);
				}
			}
		} // imageUpdated가 false인 경우, 넘어감

		clothesRepository.save(clothes);

		// 응답 정의
		RootDTO.res res = new RootDTO.res();
		res.setClothesId(clothes.getId());

		return res;
	}

	public void deleteRoot(Long clothesId) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		if (!clothesRepository.existsByIdAndMember(clothesId, member)) {
			throw new CustomException(ResponseCode.COM4010, "요청된 자원에 접근할 수 없는 계정입니다: " + member.getName());
		}

		// 요청된 옷 엔티티 획득
		Clothes clothes = clothesRepository.findById(clothesId).get();

		// 삭제할 시 연결된 coordi 데이터에 영향이 갈 수 있으므로, isDeleted 값만 변경
		clothes.setIsDeleted(true);
	}
}
