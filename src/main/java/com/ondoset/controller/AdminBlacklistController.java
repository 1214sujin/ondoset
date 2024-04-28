package com.ondoset.controller;

import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.admin.blacklist.GetRootDTO;
import com.ondoset.dto.admin.blacklist.PutRootDTO;
import com.ondoset.dto.admin.blacklist.ReporterDTO;
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
}
