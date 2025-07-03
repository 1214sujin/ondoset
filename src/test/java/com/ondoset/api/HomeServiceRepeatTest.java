package com.ondoset.api;

import com.ondoset.controller.advice.CustomException;
import com.ondoset.domain.Member;
import com.ondoset.dto.clothes.HomeDTO;
import com.ondoset.jwt.CustomUserDetails;
import com.ondoset.service.HomeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
public class HomeServiceRepeatTest {

	static long startTime;
	static double timeSum = 0;
	static double timeSquareSum = 0;
	static int successCount = 0;
	static final int REPETITION = 100;

	static final double LAT_OF_SEOUL = 37.5642135;
	static final double LON_OF_SEOUL = 127.0016985;
	static final long EPOCH_TIME_OF_TODAY = Instant.now().toEpochMilli() / 1000;

	@Autowired
	HomeService homeService;

	@BeforeEach
	void setMember() {
		Member member = new Member();
		member.setName("member1");
		CustomUserDetails customUserDetails = new CustomUserDetails(member);
		Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, null);
		SecurityContextHolder.getContext().setAuthentication(authToken);
	}

	@BeforeEach
	void getStartTime() {
		startTime = System.currentTimeMillis();
	}

	@AfterEach
	void getEndTime() {
		long endTime = System.currentTimeMillis();
		double time = endTime - startTime;
		timeSum += time;
		timeSquareSum += time * time;
	}

	@AfterAll
	static void printTimeAvg() {
		System.out.println("개선 후");
		System.out.println(REPETITION + "회 평균 응답 시간: " + timeSum/ REPETITION + " ms");
		System.out.println("표준 편차: " + Math.sqrt((timeSquareSum - (timeSum * timeSum) / REPETITION) / (REPETITION - 1)));
		System.out.println("\n테스트 통과 횟수: " + successCount);
	}

	@DisplayName("Home API 정상 응답 빈도 테스트")
	@RepeatedTest(REPETITION)
	void homeServiceResponseTest(RepetitionInfo repetitionInfo) {
		int current = repetitionInfo.getCurrentRepetition();
		System.out.println("==================== " + current + "회차 테스트 시작 ====================");

		//given
		HomeDTO.req req = new HomeDTO.req();
		req.setLat(LAT_OF_SEOUL);
		req.setLon(LON_OF_SEOUL);
		req.setDate(EPOCH_TIME_OF_TODAY);

		try{
			//when
			HomeDTO.res res = homeService.getHome(req);

			//then
			Double now = res.getForecast().getNow();
			assertThat(now).isNotNull();
			successCount += 1;
		} catch (CustomException e) {
			fail("예외 메시지: " + e.getMessage());
		}
	}
}
