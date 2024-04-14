package com.ondoset.controller;

import com.ondoset.controller.Advice.ResponseCode;
import com.ondoset.controller.Advice.ResponseMessage;
import com.ondoset.dto.OOTD.MyProfileDTO;
import com.ondoset.dto.OOTD.MyProfilePageDTO;
import com.ondoset.service.OOTDService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/ootd")
public class OOTDController {

	private final OOTDService ootdService;

	@GetMapping("/my-profile")
	public ResponseEntity<ResponseMessage<MyProfileDTO>> getMyProfile() {

		MyProfileDTO res = ootdService.getMyProfile();

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/my-profile/page")
	public ResponseEntity<ResponseMessage<MyProfilePageDTO.res>> getMyProfilePage(MyProfilePageDTO.req req) {

		MyProfilePageDTO.res res = ootdService.getMyProfilePage(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}
}
