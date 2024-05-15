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

	private final LogRepository logRepository;
	private final Kma kma;
	@Value("${com.ondoset.ai.path}")
	private String aiPath;
	@Value("{com.ondoset.pred.path}")
	private String predPath;
	private final Gson gson = new GsonBuilder().serializeNulls().create();

	private String pythonProcessExecutor(String... avg) {

		try {
			Process process = new ProcessBuilder(avg).start();

			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder output = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				output.append(line).append("\n");
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

			return output.toString();
		} catch (IOException e) {
			throw new CustomException(ResponseCode.COM5000);
		}
	}

	// AI 추천 코디
	public List<List<List<Long>>> getRecommend(Double tempAvg, Long memberId) {

		String tempRange;
		if (tempAvg > 28.2) tempRange = "9";
		else if (tempAvg > 23.2) tempRange = "8";
		else if (tempAvg > 18.2) tempRange = "7";
		else if (tempAvg > 13.2) tempRange = "6";
		else if (tempAvg > 8.2) tempRange = "5";
		else if (tempAvg > 3.2) tempRange = "4";
		else if (tempAvg > -1.8) tempRange = "3";
		else if (tempAvg > -6.8) tempRange = "2";
		else if (tempAvg > -11.8) tempRange = "1";
		else tempRange = "0";

		try {
			BufferedReader br = new BufferedReader(new FileReader(String.format("%s/user_%s/predictions_0.%s.txt", predPath, memberId.toString(), tempRange)));
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

		// 로그를 조회하여 tempAvg를 획득
		Double tempAvg = logRepository.findTempAvgByUser(member.getName());

		List<Long> tagList = new ArrayList<>();
		List<String> thicknessList = new ArrayList<>();
		for (FullTagDTO fullTag : fullTagList) {
			tagList.add(fullTag.getTagId());
			thicknessList.add(fullTag.getThickness());
		}

		// 문자열로 받음
		String satisfaction = pythonProcessExecutor("python", String.format("%s/%s", aiPath, "satisfaction.py"), member.getId().toString(), tempAvg.toString(), tagList.toString(), thicknessList.toString());
		Satisfaction res = Satisfaction.valueOfLower(satisfaction.trim());
		if (res.equals(Satisfaction.VERY_COLD)) res = Satisfaction.COLD;
		else if (res.equals(Satisfaction.VERY_HOT)) res = Satisfaction.HOT;

		return res;
	}

	// 유사 사용자
	public List<Long> getSimilarUser(Long memberId) {

		String result = pythonProcessExecutor("python", String.format("%s/%s", aiPath, "similar_user.py"), memberId.toString());

		Type type = new TypeToken<List<Long>>(){}.getType();

		return gson.fromJson(result, type);
	}

	// 날씨 비슷한 과거
	public List<Long> getSimilarDate(Member member, Double lat, Double lon, Long date) {

		Map<String, String> xy = kma.getXY(lat, lon);
		String x = xy.get("x");
		String y = xy.get("y");

		String result = pythonProcessExecutor("python", String.format("%s/%s", aiPath, "climate.py"), member.getId().toString(), x, y, date.toString());

		Type type = new TypeToken<List<Long>>(){}.getType();

		return gson.fromJson(result, type);
	}
}
