package com.ondoset.service;

import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.Member;
import com.ondoset.domain.OnBoarding;
import com.ondoset.dto.member.*;
import com.ondoset.repository.MemberRepository;
import com.ondoset.repository.OnBoardingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final OnBoardingRepository onBoardingRepository;
	@Value("${com.ondoset.resources.path}")
	private String resourcesPath;

	public UsableIdDTO.res getUsableId(UsableIdDTO.req req) {

		String name = req.getUsername();
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

	public void postRegister(RegisterDTO req) {

		String name = req.getUsername();
		String password = req.getPassword();
		String nickname = req.getNickname();

		Member data = new Member();
		data.setName(name);
		data.setPassword(passwordEncoder.encode(password));
		data.setNickname(nickname);

		try {
			memberRepository.save(data);
		}
		catch (DataIntegrityViolationException e) {
			throw new CustomException(ResponseCode.COM4090);
		}

		log.info("new member is registered. username: {}", name);
	}

	public void postOnBoarding(OnBoardingDTO req) {

		String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
		Member member = memberRepository.findByName(memberId);

		if (member.getOnBoarding() != null) {
			throw new CustomException(ResponseCode.COM4090);
		}

		OnBoarding data = new OnBoarding();
		data.setMember(member);

		Integer[] answer = req.getAnswer();
		data.setAge(answer[0]);
		data.setSex(answer[1]);
		data.setHeight(answer[2]);
		data.setWeight(answer[3]);
		data.setActivation(answer[4]);
		data.setExposure(answer[5]);

		try {

			onBoardingRepository.save(data);
		}
		catch (DataIntegrityViolationException e) {

			throw new CustomException(ResponseCode.COM4000, "허용되지 않는 범위의 답변이 포함되어 있습니다.");
		}
		finally {

			member.setOnBoarding(data);
			memberRepository.save(member);
			log.info("onBoarding data saved. username: {}", memberId);
		}
	}

	public void postProfilePic(ProfilePicDTO req) {

		MultipartFile pic = req.getImage();
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 기존에 존재하던 이미지 파일이 있다면 삭제
		String existingImage = member.getProfileImage();
		if (existingImage != null) new File(resourcesPath+existingImage).delete();

		if (pic == null || pic.isEmpty()) {

			member.setProfileImage(null);
		}
		else {

			String filename = "/profile/"+ UUID.randomUUID() +"_"+pic.getOriginalFilename();
			Path savePath = Paths.get(resourcesPath+filename);

			try {
				pic.transferTo(savePath);
				member.setProfileImage(filename);
			}
			catch (Exception e) {
				throw new CustomException(ResponseCode.COM4150);
			}
		}
		memberRepository.save(member);
	}

	public void postNickname(NicknameDTO req) {

		String newNickname = req.getNickname();
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		try {
			member.setNickname(newNickname);
			memberRepository.save(member);
		}
		catch (DataIntegrityViolationException e) {
			throw new CustomException(ResponseCode.COM4090);
		}
	}

	public void getDelete() {

		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// 기존에 존재하던 이미지 파일이 있다면 삭제
		String existingImage = member.getProfileImage();
		if (existingImage != null) new File(resourcesPath+existingImage).delete();

		try {
			memberRepository.delete(member);
		} catch (InvalidDataAccessApiUsageException e) {
			throw new CustomException(ResponseCode.COM4091);
		}
	}
}
