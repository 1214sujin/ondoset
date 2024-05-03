package com.ondoset.service;

import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.Enum.Category;
import com.ondoset.domain.Tag;
import com.ondoset.dto.admin.tag.TagDTO;
import com.ondoset.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Log4j2
@RequiredArgsConstructor
@Service
public class AdminTagService {

	private final TagRepository tagRepository;

	public com.ondoset.dto.clothes.TagDTO getRoot() {

		com.ondoset.dto.clothes.TagDTO res = new com.ondoset.dto.clothes.TagDTO();
		res.setTop(tagRepository.findTop());
		res.setBottom(tagRepository.findBottom());
		res.setOuter(tagRepository.findOuter());
		res.setShoe(tagRepository.findShoe());
		res.setAcc(tagRepository.findAcc());

		return res;
	}

	public TagDTO.res postRoot(TagDTO.req req) {

		Tag tag = new Tag();
		tag.setCategory(Category.valueOfLower(req.getCategory()));
		tag.setName(req.getTag());

		TagDTO.res res = new TagDTO.res();
		res.setTagId(tagRepository.save(tag).getId());

		return res;
	}

	public TagDTO.res putRoot(Long tagId, TagDTO.req req) {

		Tag tag = tagRepository.findById(tagId).get();
		tag.setCategory(Category.valueOfLower(req.getCategory()));
		tag.setName(req.getTag());

		TagDTO.res res = new TagDTO.res();
		res.setTagId(tagRepository.save(tag).getId());

		return res;
	}

	public void deleteRoot(Long tagId) {

		try {
			tagRepository.deleteById(tagId);
		} catch (DataIntegrityViolationException e) {
			throw new CustomException(ResponseCode.COM4000, "이미 사용자가 사용한 태그를 삭제할 수 없습니다.");
		}
	}
}
