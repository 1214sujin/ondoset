package com.ondoset.controller;

import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.member.*;
import com.ondoset.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<ResponseMessage<UsableIdDTO.res>> getUsableId(UsableIdDTO.req req) {

		UsableIdDTO.res res = memberService.getUsableId(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@GetMapping("/usable-nickname")
	public ResponseEntity<ResponseMessage<UsableNicknameDTO.res>> getUsableNickname(UsableNicknameDTO.req req) {

		UsableNicknameDTO.res res = memberService.getUsableNickname(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PostMapping("/register")
	public ResponseEntity<ResponseMessage<String>> postRegister(@RequestBody RegisterDTO req) {

		memberService.postRegister(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "회원가입 성공"));
	}

	@PostMapping("/on-boarding")
	public ResponseEntity<ResponseMessage<String>> postOnBoarding(@RequestBody OnBoardingDTO req) {

		memberService.postOnBoarding(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "저장 성공"));
	}

	@PostMapping("/profile-pic")
	public ResponseEntity<ResponseMessage<String>> postProfilePic(ProfilePicDTO req) {

		memberService.postProfilePic(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "변경 성공"));
	}

	@PostMapping("/nickname")
	public ResponseEntity<ResponseMessage<String>> postNickname(@RequestBody NicknameDTO req) {

		memberService.postNickname(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "변경 성공"));
	}

	@GetMapping("/delete")
	public ResponseEntity<ResponseMessage<String>> getDelete() {

		memberService.getDelete();

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, "회원탈퇴 성공"));
	}
}
