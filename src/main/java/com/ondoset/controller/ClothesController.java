package com.ondoset.controller;

import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.clothes.HomeDTO;
import com.ondoset.service.ClothesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/clothes")
public class ClothesController {

	private final ClothesService clothesService;

	@GetMapping("/home")
	public ResponseEntity<ResponseMessage<HomeDTO.res>> getHome(@Valid @RequestBody HomeDTO.req req) {

		HomeDTO.res res = clothesService.getHome(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}
}
