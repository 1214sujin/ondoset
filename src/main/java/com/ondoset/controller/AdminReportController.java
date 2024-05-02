package com.ondoset.controller;

import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.admin.report.ReportDTO;
import com.ondoset.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/report")
public class AdminReportController {

	private final AdminReportService adminReportService;

	@GetMapping({"/", ""})
	public ResponseEntity<ResponseMessage<List<ReportDTO>>> getRoot() {

		List<ReportDTO> res = adminReportService.getRoot();

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/count/{ootdId}")
	public ResponseEntity<ResponseMessage<String>> putCount(@PathVariable("ootdId") Long ootdId) {

		adminReportService.putCount(ootdId);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "초기화 성공"));
	}
}
