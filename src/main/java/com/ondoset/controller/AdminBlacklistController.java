package com.ondoset.controller;

import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.admin.blacklist.GetRootDTO;
import com.ondoset.dto.admin.blacklist.PutRootDTO;
import com.ondoset.service.AdminService;
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

	private final AdminService adminService;

	@GetMapping({"/", ""})
	public ResponseEntity<ResponseMessage<List<GetRootDTO>>> getRoot() {

		List<GetRootDTO> res = adminService.getRoot();

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PutMapping("/{memberId}")
	public ResponseEntity<ResponseMessage<PutRootDTO.res>> putRoot(@PathVariable("memberId") Long memberId, @Valid @RequestBody PutRootDTO.req req) {

		PutRootDTO.res res = adminService.putRoot(memberId, req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}
}
