package com.ondoset.controller;

import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.member.*;
import com.ondoset.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/test")
	public ResponseEntity<String> test() {

		log.info("MemberController.test");
		return ResponseEntity.ok("ok");
	}

	@GetMapping("/usable-id")
	public ResponseEntity<ResponseMessage<UsableIdDTO.res>> getUsableId(@Valid UsableIdDTO.req req, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) throw new CustomException(ResponseCode.COM4000);

		UsableIdDTO.res res = memberService.getUsableId(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/usable-nickname")
	public ResponseEntity<ResponseMessage<UsableNicknameDTO.res>> getUsableNickname(@Valid UsableNicknameDTO.req req, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) throw new CustomException(ResponseCode.COM4000);

		UsableNicknameDTO.res res = memberService.getUsableNickname(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PostMapping("/register")
	public ResponseEntity<ResponseMessage<String>> postRegister(@Valid @RequestBody RegisterDTO req) {

		memberService.postRegister(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "회원가입 성공"));
	}

	@PostMapping("/on-boarding")
	public ResponseEntity<ResponseMessage<String>> postOnBoarding(@Valid @RequestBody OnBoardingDTO req) {

		memberService.postOnBoarding(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "저장 성공"));
	}

	@PostMapping("/profile-pic")
	public ResponseEntity<ResponseMessage<String>> postProfilePic(ProfilePicDTO req) {

		memberService.postProfilePic(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "변경 성공"));
	}

	@PostMapping("/nickname")
	public ResponseEntity<ResponseMessage<String>> postNickname(@Valid @RequestBody NicknameDTO req) {

		memberService.postNickname(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "변경 성공"));
	}

	@GetMapping("/delete")
	public ResponseEntity<ResponseMessage<String>> getDelete() {

		memberService.getDelete();

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "회원탈퇴 성공"));
	}
}
