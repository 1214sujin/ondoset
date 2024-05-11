package com.ondoset.controller;

import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.domain.Enum.Category;
import com.ondoset.domain.Enum.Thickness;
import com.ondoset.dto.clothes.*;
import com.ondoset.service.ClothesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/clothes")
public class ClothesController {

	private final ClothesService clothesService;

	@GetMapping("/home")
	public ResponseEntity<ResponseMessage<HomeDTO.res>> getHome(@Valid HomeDTO.req req) {

		HomeDTO.res res = clothesService.getHome(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("")
	public ResponseEntity<ResponseMessage<SearchTagDTO.res>> getSearchTag(@Valid SearchTagDTO.req req) {

		SearchTagDTO.res res = clothesService.getSearchTag(Thickness.valueOfLower(req.getThickness()), req.getTagId());

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/all")
	public ResponseEntity<ResponseMessage<AllDTO.res>> getAll(AllDTO.req req) {

		AllDTO.res res = clothesService.getAll(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/search")
	public ResponseEntity<ResponseMessage<SearchNameDTO.res>> getSearch(@Valid SearchNameDTO.req req) {

		SearchNameDTO.res res;
		try {
			Category category = Category.valueOfLower(req.getCategory());
			res = clothesService.getSearch(category, req.getClothesName());
		} catch (NullPointerException e) {
			res = clothesService.getSearch(null, req.getClothesName());
		} catch (IllegalArgumentException e) {
			throw new CustomException(ResponseCode.COM4000, "category: 널일 수 없습니다.");
		}

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/tag")
	public ResponseEntity<ResponseMessage<TagDTO>> getTag() {

		TagDTO res = clothesService.getTag();

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PostMapping({"/", ""})
	public ResponseEntity<ResponseMessage<RootDTO.res>> postRoot(@Valid RootDTO.req req) {

		RootDTO.res res = clothesService.postRoot(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PutMapping("/{clothesId}")
	public ResponseEntity<ResponseMessage<RootDTO.res>> putRoot(@PathVariable("clothesId") Long clothesId, @Valid RootDTO.putReq req) {

		RootDTO.res res = clothesService.putRoot(clothesId, req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PatchMapping("/{clothesId}")
	public ResponseEntity<ResponseMessage<RootDTO.res>> patchRoot(@PathVariable("clothesId") Long clothesId, @Valid RootDTO.patchReq req) {

		RootDTO.res res = clothesService.patchRoot(clothesId, req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@DeleteMapping("/{clothesId}")
	public ResponseEntity<ResponseMessage<String>> deleteRoot(@PathVariable("clothesId") Long clothesId) {

		clothesService.deleteRoot(clothesId);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "삭제 성공"));
	}
}
