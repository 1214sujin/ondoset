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
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
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
	public List<List<List<Long>>> getRecommend(Double tempAvg, Member member) {

		//[[태그, 태그, 태그], [두께감, null, null]] * 3
		// -1: THIN, 0: NORMAL, 1: THICK
		String result = pythonProcessExecutor("python", String.format("%s/%s", aiPath, "test.py"), member.getId().toString(), tempAvg.toString());

		Type type = new TypeToken<List<List<List<Long>>>>(){}.getType();
		return gson.fromJson(result, type);
	}

	// 만족도 예측
	public Satisfaction getSatisfaction(Member member, List<FullTagDTO> fullTagList) {

		// 로그를 조회하여 tempAvg를 획득
		Double tempAvg = logRepository.findTempAvgByUser(member.getName());

		return Satisfaction.GOOD;
	}

	// 유사 사용자
	public List<Long> getSimilarUser(Long memberId) {

		return Arrays.asList(2L, 3L);
	}

	// 날씨 비슷한 과거
	public List<Long> getSimilarDate(Double lat, Double lon, Long date) {

		Map<String, String> xy = kma.getXY(lat, lon);
		String x = xy.get("x");
		String y = xy.get("y");

		return Arrays.asList(1713193200L, 1713279600L, 1713366000L, 1713452400L);
	}
}
