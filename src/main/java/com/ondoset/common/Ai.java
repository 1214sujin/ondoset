package com.ondoset.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.Enum.Satisfaction;
import com.ondoset.domain.Member;
import com.ondoset.dto.coordi.FullTagDTO;
import com.ondoset.repository.LogRepository;
import com.ondoset.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
@Component
public class Ai {

	private final MemberRepository memberRepository;
	private final LogRepository logRepository;
	@Value("${com.ondoset.ai.path}")
	private String aiPath;
	@Value("${com.ondoset.pred.path}")
	private String predPath;
	@Value("${com.ondoset.python.path}")
	private String pythonPath;
	private final Gson gson = new GsonBuilder().serializeNulls().create();

	private String pythonProcessExecutor(Boolean isList, String... avg) {

		try {
			Process process = new ProcessBuilder(avg).start();

			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder output = new StringBuilder();
			String line;

			if (isList) {

				while ((line = br.readLine()) != null) {
					if (line.startsWith("[")) output.append(line).append("\n");
					else log.debug(line);
				}
			} else {

				while ((line = br.readLine()) != null) {
					if (Character.isUpperCase(line.charAt(0))) output.append(line).append("\n");
					else log.debug(line);
				}
			}

			// 파이썬 실행 결과 오류가 존재할 경우
			if (output.isEmpty()) {
				br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				output = new StringBuilder();

				while ((line = br.readLine()) != null) {
					output.append(line).append("\n");
				}
				throw new CustomException(ResponseCode.AI5002, output.toString());
			}
			br.close();
			process.destroy();

			return output.toString().trim();
		} catch (IOException e) {
			throw new CustomException(ResponseCode.COM5000);
		}
	}

	private String reqIdOf(Long memberId) {

		// 뉴비인지 확인
		String isNewbie = pythonProcessExecutor(false, pythonPath, String.format("%s/%s", aiPath, "is_new.py"), memberId.toString());

		if (isNewbie.equals("NEW")) {
			return "0";
		} else {
			return memberId.toString();
		}
	}

	// AI 추천 코디
	public List<List<List<Long>>> getRecommend(Double tempAvg, Long memberId) {

		String tempRange;
		if (tempAvg > 28.2) tempRange = "10";
		else if (tempAvg > 23.2) tempRange = "9";
		else if (tempAvg > 18.2) tempRange = "8";
		else if (tempAvg > 13.2) tempRange = "7";
		else if (tempAvg > 8.2) tempRange = "6";
		else if (tempAvg > 3.2) tempRange = "5";
		else if (tempAvg > -1.8) tempRange = "4";
		else if (tempAvg > -6.8) tempRange = "3";
		else if (tempAvg > -11.8) tempRange = "2";
		else tempRange = "1";

		try {
			BufferedReader br = new BufferedReader(new FileReader(String.format("%s/user_%s/predictions_%s.0.txt", predPath, reqIdOf(memberId), tempRange)));
			List<String> fileContent = new ArrayList<>();
			String line;

			while ((line = br.readLine()) != null) {
				fileContent.add(line);
			}
			br.close();

			List<List<List<Long>>> res = new ArrayList<>();
			Type type = new TypeToken<List<Long>>(){}.getType();

			int recommendCount = fileContent.size() / 2;
			for (int i = 0; i < recommendCount; i++) {
				res.add(Arrays.asList(gson.fromJson(fileContent.get(i), type), gson.fromJson(fileContent.get(recommendCount + i), type)));
			}

			return res;

		} catch (IOException e) {
			e.printStackTrace();
			throw new CustomException(ResponseCode.COM5000);
		}
	}

	// 만족도 예측
	public Satisfaction getSatisfaction(Member member, List<FullTagDTO> fullTagList) {

		// 사용자가 최근에 받은 tempAvg를 획득
		Double tempAvg = member.getRecentReqTemp();

		List<Long> tagList = new ArrayList<>();
		List<String> thicknessList = new ArrayList<>();
		for (FullTagDTO fullTag : fullTagList) {
			tagList.add(fullTag.getTagId());
			thicknessList.add(fullTag.getThickness());
		}

		// 문자열로 받음
		String satisfaction = pythonProcessExecutor(false, pythonPath, String.format("%s/%s", aiPath, "satisfaction.py"), reqIdOf(member.getId()), tempAvg.toString(), tagList.toString(), thicknessList.toString());
		Satisfaction res = Satisfaction.valueOfLower(satisfaction);
		if (res.equals(Satisfaction.VERY_COLD)) res = Satisfaction.COLD;
		else if (res.equals(Satisfaction.VERY_HOT)) res = Satisfaction.HOT;

		return res;
	}

	// 유사 사용자
	public List<Long> getSimilarUser(Long memberId) {

		if (reqIdOf(memberId).equals("0")) {
			return new ArrayList<>();
		}

		String result = pythonProcessExecutor(true, pythonPath, String.format("%s/%s", aiPath, "similar_user.py"), memberId.toString());

		Type type = new TypeToken<List<Long>>(){}.getType();
		List<Long> res = gson.fromJson(result, type);

		res.removeIf(id -> !memberRepository.existsById(id));

		return res;
	}

	// 날씨 비슷한 과거
	public List<Long> getSimilarDate(Member member, Map<String, String> xy, Long nowTimestamp, Integer daysFromToday) {

		String result = pythonProcessExecutor(true, pythonPath, String.format("%s/%s", aiPath, "climate.py"),
				member.getId().toString(), xy.get("x"), xy.get("y"), nowTimestamp.toString(), daysFromToday.toString());

		Type type = new TypeToken<List<Long>>(){}.getType();

		return gson.fromJson(result, type);
	}
}
