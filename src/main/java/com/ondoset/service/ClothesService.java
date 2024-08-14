package com.ondoset.service;

import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.*;
import com.ondoset.domain.Enum.Category;
import com.ondoset.domain.Enum.Thickness;
import com.ondoset.domain.Tag;
import com.ondoset.dto.clothes.*;
import com.ondoset.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Log4j2
@RequiredArgsConstructor
@Service
@Transactional
public class ClothesService {

	private final MemberRepository memberRepository;
	private final ClothesRepository clothesRepository;
	private final TagRepository tagRepository;
	@Value("${com.ondoset.resources.path}")
	private String resourcesPath;

	public SearchTagDTO.res getSearchTag(Thickness thickness, Long tagId) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 카테고리에 따라 분기 처리
		Tag tag = tagRepository.findById(tagId).get();

		SearchTagDTO.res res = new SearchTagDTO.res();

		List<Clothes> clothesList;
		if (Arrays.asList(Category.SHOE, Category.ACC).contains(tag.getCategory())) {

			clothesList = clothesRepository.findByFullTag(member, tag);
			res.setCoupangURL("https://www.coupang.com/np/search?q=" + tag.getName());
		} else {

			clothesList = clothesRepository.findByFullTag(member, thickness, tag);
			String query = String.join(" ", thickness.getName(), tag.getName());
			res.setCoupangURL("https://www.coupang.com/np/search?q=" + query);
		}

		res.setClothesShortList(new ArrayList<>());
		for (Clothes c : clothesList) {

			res.getClothesShortList().add(new ClothesShortDTO(c.getId(), c.getName(), c.getImageURL()));
		}

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

	public SearchNameDTO.res getSearch(Category category, String clothesName) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 전체 카테고리에 대한 조회인지 검사 및 분기 처리
		SearchNameDTO.res res = new SearchNameDTO.res();
		List<ClothesDTO> clothesList;
		if (category == null) {
			clothesList = clothesRepository.findBySearch(member, clothesName);
		} else {
			clothesList = clothesRepository.findBySearch(member, category, clothesName);
		}
		res.setClothesList(clothesList);

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
		clothes.setName(req.getName());

		Tag tag = tagRepository.findById(req.getTagId()).get();
		clothes.setTag(tag);

		// thickness를 필수로 받는 카테고리라면, thickness가 정상적으로 요청되었는지 검사
		Thickness thickness = null;
		if (!Arrays.asList(Category.SHOE, Category.ACC).contains(tag.getCategory())) {
			if (req.getThickness() == null) {
				throw new CustomException(ResponseCode.COM4000, "thickness가 필요한 카테고리입니다.");
			} else {
				thickness = Thickness.valueOfLower(req.getThickness());
			}
		}
		clothes.setThickness(thickness);

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
		clothes.setName(req.getName());

		Tag tag = tagRepository.findById(req.getTagId()).get();
		clothes.setTag(tag);

		// thickness를 필수로 받는 카테고리라면, thickness가 정상적으로 요청되었는지 검사
		Thickness thickness = null;
		if (!Arrays.asList(Category.SHOE, Category.ACC).contains(tag.getCategory())) {
			if (req.getThickness() == null) {
				throw new CustomException(ResponseCode.COM4000, "thickness가 필요한 카테고리입니다.");
			} else {
				thickness = Thickness.valueOfLower(req.getThickness());
			}
		}
		clothes.setThickness(thickness);

		// 이미지 처리
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

		clothesRepository.save(clothes);

		// 응답 정의
		RootDTO.res res = new RootDTO.res();
		res.setClothesId(clothes.getId());

		return res;
	}

	public RootDTO.res patchRoot(Long clothesId, RootDTO.patchReq req) {

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
		clothes.setName(req.getName());

		Tag tag = tagRepository.findById(req.getTagId()).get();
		clothes.setTag(tag);

		// thickness를 필수로 받는 카테고리라면, thickness가 정상적으로 요청되었는지 검사
		Thickness thickness = null;
		if (!Arrays.asList(Category.SHOE, Category.ACC).contains(tag.getCategory())) {
			if (req.getThickness() == null) {
				throw new CustomException(ResponseCode.COM4000, "thickness가 필요한 카테고리입니다.");
			} else {
				thickness = Thickness.valueOfLower(req.getThickness());
			}
		}
		clothes.setThickness(thickness);

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
