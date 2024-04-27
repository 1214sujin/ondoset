package com.ondoset.controller;

import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.coordi.*;
import com.ondoset.service.CoordiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/coordi")
public class CoordiController {

	private final CoordiService coordiService;

	@PostMapping({"/", ""})
	public ResponseEntity<ResponseMessage<DateDTO>> postRoot(@Valid @RequestBody PostRootDTO req) {

		DateDTO res = coordiService.postRoot(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PostMapping("/plan")
	public ResponseEntity<ResponseMessage<DateDTO>> postPlan(@Valid @RequestBody PlanDTO req) {

		DateDTO res = coordiService.postPlan(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping({"/", ""})
	public ResponseEntity<ResponseMessage<List<GetRootDTO.res>>> getRoot(@Valid GetRootDTO.req req, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) throw new CustomException(ResponseCode.COM4000);

		List<GetRootDTO.res> res = coordiService.getRoot(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PutMapping("/image/{coordiId}")
	public ResponseEntity<ResponseMessage<DateDTO>> putImage(@PathVariable("coordiId") Long coordiId, ImageDTO req) {

		DateDTO res = coordiService.putImage(coordiId, req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PutMapping("/{coordiId}")
	public ResponseEntity<ResponseMessage<DateDTO>> putRoot(@PathVariable("coordiId") Long coordiId, @Valid @RequestBody PutRootDTO req) {

		DateDTO res = coordiService.putRoot(coordiId, req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@DeleteMapping("/{coordiId}")
	public ResponseEntity<ResponseMessage<String>> deleteRoot(@PathVariable("coordiId") Long coordiId) {

		coordiService.deleteRoot(coordiId);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "삭제 성공"));
	}

	@PutMapping("/out-time/{coordiId}")
	public ResponseEntity<ResponseMessage<DateDTO>> putOutTime(@PathVariable("coordiId") Long coordiId, @Valid @RequestBody OutTimeDTO req) {

		DateDTO res = coordiService.putOutTime(coordiId, req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PutMapping("/satisfaction/{coordiId}")
	public ResponseEntity<ResponseMessage<DateDTO>> putSatisfaction(@PathVariable("coordiId") Long coordiId, @Valid @RequestBody SatisfactionDTO req) {

		DateDTO res = coordiService.putSatisfaction(coordiId, req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}
}
