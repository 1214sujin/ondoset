package com.ondoset.service;

import com.ondoset.domain.Member;
import com.ondoset.dto.Member.*;
import com.ondoset.jwt.JWTUtil;
import com.ondoset.repository.MemberRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

	private final MemberRepository memberRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final JWTUtil jwtUtil;

	public UsableIdDTO.res getUsableId(UsableIdDTO.req req) {

		String name = req.getMemberId();
		log.info("memberId: {}", name);
		UsableIdDTO.res res = new UsableIdDTO.res();

		Boolean isNameExist = memberRepository.existsByName(name);

		if (isNameExist) {

			res.setUsable(false);
			res.setMsg("이미 사용 중인 아이디입니다.");
		}
		else {

			res.setUsable(true);
			res.setMsg("사용 가능한 아이디입니다.");
		}
		return res;
	}

	public UsableNicknameDTO.res getUsableNickname(UsableNicknameDTO.req req) {

		String nickname = req.getNickname();
		log.info("nickname: {}", nickname);
		UsableNicknameDTO.res res = new UsableNicknameDTO.res();

		Boolean isNicknameExist = memberRepository.existsByNickname(nickname);

		if (isNicknameExist) {

			res.setUsable(false);
			res.setMsg("이미 사용 중인 닉네임입니다.");
		}
		else {

			res.setUsable(true);
			res.setMsg("사용 가능한 닉네임입니다.");
		}

		return res;
	}

	public String postRegister(RegisterDTO req) {

		String name = req.getMemberId();
		String password = req.getPassword();
		String nickname = req.getNickname();

		Member data = new Member();

		data.setName(name);
		data.setPassword(passwordEncoder.encode(password));
		data.setNickname(nickname);

		memberRepository.save(data);

		return "회원가입 성공";
	}
}
