package com.ondoset.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ondoset.common.Kma;
import com.ondoset.dto.kma.PastWDTO;
import com.ondoset.repository.MemberRepository;
import com.ondoset.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Log4j2
@RequiredArgsConstructor
@Service
public class AdminMonitorService {

	private final TagRepository tagRepository;
	private final Kma kma;
	@Value("${com.ondoset.data.service_key}")
	private String serviceKey;

	public void getDb() throws Exception {

		tagRepository.findById(1L).get();
	}

	public void getWeather() throws Exception {

		// 어제 날씨가 제대로 조회 되는지 확인
		String reqTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		URL url = new URL(String.format("https://apis.data.go.kr/1360000/AsosHourlyInfoService/getWthrDataList" +
						"?numOfRows=1&pageNo=1&dataType=JSON&dataCd=ASOS&dateCd=HR&startDt=%s&startHh=00&endDt=%1$s&endHh=00&stnIds=108&serviceKey=%s",
				reqTime, serviceKey));

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/json");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String response = in.readLine();

		in.close();
		con.disconnect();

		JsonObject result = JsonParser.parseString(response).getAsJsonObject();
		String kmaErrorCode = result.getAsJsonObject("response").getAsJsonObject("header").get("resultCode").toString().replace("\"", "");
		if (!kmaErrorCode.equals("00")) throw new Exception();
	}
}
