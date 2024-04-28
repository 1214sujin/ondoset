package com.ondoset.controller;

import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.admin.tag.TagDTO;
import com.ondoset.service.AdminTagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/tag")
public class AdminTagController {

	private final AdminTagService adminTagService;

	@GetMapping({"/", ""})
	public ResponseEntity<ResponseMessage<com.ondoset.dto.clothes.TagDTO>> getRoot() {

		com.ondoset.dto.clothes.TagDTO res = adminTagService.getRoot();

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PostMapping({"/", ""})
	public ResponseEntity<ResponseMessage<TagDTO.res>> postRoot(@Valid @RequestBody TagDTO.req req) {

		TagDTO.res res = adminTagService.postRoot(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PutMapping("/{tagId}")
	public ResponseEntity<ResponseMessage<TagDTO.res>> putRoot(@PathVariable("tagId") Long tagId, @Valid @RequestBody TagDTO.req req) {

		TagDTO.res res = adminTagService.putRoot(tagId, req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@DeleteMapping("/{tagId}")
	public ResponseEntity<ResponseMessage<String>> deleteRoot(@PathVariable("tagId") Long tagId) {

		adminTagService.deleteRoot(tagId);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "삭제 성공"));
	}
}
