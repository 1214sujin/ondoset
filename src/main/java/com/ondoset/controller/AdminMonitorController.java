package com.ondoset.controller;

import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.service.AdminMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/monitor")
public class AdminMonitorController {

	private final AdminMonitorService adminMonitorService;

	@GetMapping("/db")
	public ResponseEntity<ResponseMessage<String>> getDb() {

		try {
			adminMonitorService.getDb();
			return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "Normal"));
		} catch (Exception e) {
			return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "Error"));
		}
	}

	@GetMapping("/weather")
	public ResponseEntity<ResponseMessage<String>> getWeather() {

		try {
			adminMonitorService.getWeather();
			return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "Normal"));
		} catch (Exception e) {
			return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "Error"));
		}
	}
}
