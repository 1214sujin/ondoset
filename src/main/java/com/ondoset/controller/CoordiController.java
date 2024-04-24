package com.ondoset.controller;

import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.coordi.RootDTO;
import com.ondoset.service.CoordiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/coordi")
public class CoordiController {

	private final CoordiService coordiService;

	@PostMapping({"/", ""})
	public ResponseEntity<ResponseMessage<RootDTO.res>> postRoot(@RequestBody RootDTO.req req) {

		RootDTO.res res = coordiService.postRoot(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}
}
