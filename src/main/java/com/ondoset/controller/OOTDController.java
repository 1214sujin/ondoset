package com.ondoset.controller;

import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.ootd.*;
import com.ondoset.service.OOTDService;
import jakarta.validation.Valid;
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
	public ResponseEntity<ResponseMessage<MyProfilePageDTO>> getMyProfilePage(PageDTO req) {

		MyProfilePageDTO res = ootdService.getMyProfilePage(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/weather")
	public ResponseEntity<ResponseMessage<OotdPageDTO>> getWeather(@Valid WeatherDTO req) {

		OotdPageDTO res = ootdService.getWeather(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/like-list")
	public ResponseEntity<ResponseMessage<OotdPageDTO>> getLikeList(PageDTO req) {

		OotdPageDTO res = ootdService.getLikeList(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/follow-list")
	public ResponseEntity<ResponseMessage<FollowingPageDTO>> getFollowList(PageDTO req) {

		FollowingPageDTO res = ootdService.getFollowList(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}
}
