package com.ondoset.controller;

import com.ondoset.controller.Advice.ResponseCode;
import com.ondoset.controller.Advice.ResponseMessage;
import com.ondoset.dto.Member.OnBoardingDTO;
import com.ondoset.dto.Member.RegisterDTO;
import com.ondoset.dto.Member.UsableIdDTO;
import com.ondoset.dto.Member.UsableNicknameDTO;
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

	@PostMapping("/register")        // String nickname, String memberId, String password
	public ResponseEntity<ResponseMessage<String>> postRegister(@RequestBody RegisterDTO req) {

		String res = memberService.postRegister(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}

	@PostMapping("/on-boarding")
	public ResponseEntity<ResponseMessage<String>> postOnBoarding(@RequestBody OnBoardingDTO req) {

		String res = memberService.postOnBoarding(req);

		return ResponseEntity.ok(new ResponseMessage<>(ResponseCode.COM2000, res));
	}
}
