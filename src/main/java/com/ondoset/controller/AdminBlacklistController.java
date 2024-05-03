package com.ondoset.controller;

import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.admin.blacklist.*;
import com.ondoset.service.AdminBlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/blacklist")
public class AdminBlacklistController {

	private final AdminBlacklistService adminBlacklistService;

	@GetMapping({"/", ""})
	public ResponseEntity<ResponseMessage<List<GetRootDTO>>> getRoot() {

		List<GetRootDTO> res = adminBlacklistService.getRoot();

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PutMapping("/{memberId}")
	public ResponseEntity<ResponseMessage<PutRootDTO.res>> putRoot(@PathVariable("memberId") Long memberId, @Valid @RequestBody PutRootDTO.req req) {

		PutRootDTO.res res = adminBlacklistService.putRoot(memberId, req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/reporter")
	public ResponseEntity<ResponseMessage<List<ReporterDTO>>> getReporter() {

		List<ReporterDTO> res = adminBlacklistService.getReporter();

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/reporter-list")
	public ResponseEntity<ResponseMessage<ReporterListDTO.res>> getReporterList(@Valid ReporterListDTO.req req) {

		ReporterListDTO.res res = adminBlacklistService.getReporterList(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/reported")
	public ResponseEntity<ResponseMessage<List<ReporterDTO>>> getReported() {

		List<ReporterDTO> res = adminBlacklistService.getReported();

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/reported-list")
	public ResponseEntity<ResponseMessage<ReporterListDTO.res>> getReportedList(@Valid ReporterListDTO.req req) {

		ReporterListDTO.res res = adminBlacklistService.getReportedList(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}
}
