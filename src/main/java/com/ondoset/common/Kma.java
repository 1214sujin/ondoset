package com.ondoset.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.Enum.Weather;
import com.ondoset.dto.kma.PastWDTO;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Log4j2
@Component
@NoArgsConstructor
public class Kma {

	private final DateTimeFormatter kmaFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(TimeZone.getDefault().toZoneId());
	@Value("${com.ondoset.kma.auth_key}")
	private String authKey;
	@Value("${com.ondoset.data.service_key}")
	private String serviceKey;

	// JSON으로 반환되는 API 요청
	private JsonObject getAPIRes(String reqUrl) {

		try {
			URL url = new URL(reqUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String response = in.readLine();

			in.close();
			con.disconnect();

			return JsonParser.parseString(response).getAsJsonObject();
		}
		catch (IOException e) {
			throw new CustomException(ResponseCode.COM4000, "오류가 발생한 요청 API: "+reqUrl);
		}
	}

	// 위도/경도를 관측 지점 정보로 변환
	private String getStn(Double lat, Double lon, Long epochTime) {

		// 날짜를 API 요청 형식으로 변환
		Instant instant = Instant.ofEpochSecond(epochTime);
		LocalDateTime datetime = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
		String time = datetime.format(kmaFormatter);

		String reqUrl = String.format("https://apihub.kma.go.kr/api/typ01/url/stn_inf.php?inf=SFC&tm=%s&authKey=%s", time, authKey);

		//*********** stnId 받아오기 시작 ***********
		try {
			URL url = new URL(reqUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String stn = "0";
			double min_dist = 999.9;
			String inputLine;
			// 설명 라인 버리기
			in.readLine();
			in.readLine();
			in.readLine();

			while ((inputLine = in.readLine()) != null) {
				// 라인 파싱 및 해당 라인이 마지막 라인일 경우 break
				String[] tuple = inputLine.replaceAll(" {2,}", " ").split(" ");
				if (tuple.length == 1) break;

				// 사용자가 요청한 위도/경도와의 거리 계산
				double a = Double.parseDouble(tuple[3]) - lat;
				double b = Double.parseDouble(tuple[2]) - lon;
				double dist = a * a + b * b;

				// 현재까지 중 최솟값인지 확인
				if (dist < min_dist) {
					min_dist = dist;
					stn = tuple[1];
				}
			}
			in.close();
			con.disconnect();
			//*********** stnId 받아오기 끝 ***********

			return stn;
		}
		catch (IOException e) {
			throw new CustomException(ResponseCode.COM4000, "오류가 발생한 요청 API: "+reqUrl);
		}
	}

	// 시간과 함께 요청하지 않으면 현재 시간을 가지고 관측 지점 조회
	private String getStn(Double lat, Double lon) {

		return getStn(lat, lon, Instant.now().getEpochSecond());
	}

	/** 과거 날씨 조회 */
	public PastWDTO getPastW(Double lat, Double lon, Long departTime, Long arrivalTime) {

		// 날짜를 API 요청 형식으로 변환
		Instant departInstant = Instant.ofEpochSecond(departTime);
		LocalDateTime departDatetime = LocalDateTime.ofInstant(departInstant, ZoneId.of("Asia/Seoul"));
		String departReqTime = departDatetime.format(kmaFormatter);

		Instant arrivalInstant = Instant.ofEpochSecond(arrivalTime);
		LocalDateTime arrivalDatetime = LocalDateTime.ofInstant(arrivalInstant, ZoneId.of("Asia/Seoul"));
		String arrivalReqTime = arrivalDatetime.format(kmaFormatter);

		// 사용자 위치에 대한 stn 값 획득
		String stn = getStn(lat, lon, departTime);

		// 지상 관측 시간자료 API 요청
		JsonObject response = getAPIRes(String.format("https://apis.data.go.kr/1360000/AsosHourlyInfoService/getWthrDataList" +
				"?numOfRows=48&pageNo=1&dataType=JSON&dataCd=ASOS&dateCd=HR&startDt=%s&startHh=%s&endDt=%s&endHh=%s&stnIds=%s" +
				"&serviceKey=%s", departReqTime.substring(0, 8), departReqTime.substring(8, 10), arrivalReqTime.substring(0, 8), arrivalReqTime.substring(8, 10), stn, serviceKey));

		JsonArray items = response.getAsJsonObject("response").getAsJsonObject("body").getAsJsonObject("items").getAsJsonArray("item");

		int lowestTemp = 99;
		int highestTemp= -99;

		for (JsonElement e : items.getAsJsonArray()) {
			String ta = e.getAsJsonObject().get("ta").toString().replace("\"", "");
			int tempAvg = Math.toIntExact(Math.round(Double.parseDouble(ta)));

			if (tempAvg < lowestTemp) {
				lowestTemp = tempAvg;
			}
			if (tempAvg > highestTemp) {
				highestTemp = tempAvg;
			}
		}

		// 지상 관측 일자료 API 요청
		response = getAPIRes(String.format("http://apis.data.go.kr/1360000/AsosDalyInfoService/getWthrDataList" +
				"?numOfRows=1&pageNo=1&dataType=JSON&dataCd=ASOS&dateCd=DAY&startDt=%s&endDt=%s&stnIds=%s" +
				"&serviceKey=%s", departReqTime.substring(0, 8), departReqTime.substring(0, 8), stn, serviceKey));

		items = response.getAsJsonObject("response").getAsJsonObject("body").getAsJsonObject("items").getAsJsonArray("item");

		Weather weather = null;

		for (JsonElement e : items.getAsJsonArray()) {
			String rain = e.getAsJsonObject().get("n99Rn").toString().replace("\"", "");
			String snow = e.getAsJsonObject().get("ddMefs").toString().replace("\"", "");

			if (snow.equals("")) {

				if (rain.equals("")) {

					double cloud = Double.parseDouble(e.getAsJsonObject().get("avgTca").toString().replace("\"", ""));
					if (cloud <= 5) {
						weather = Weather.SUNNY;
					} else if (cloud <= 8) {
						weather = Weather.PARTLY_CLOUDY;
					} else {
						weather = Weather.CLOUDY;
					}
				} else {
					weather = Weather.RAINY;
				}
			} else {
				weather = Weather.SNOWY;
			}
		}

		PastWDTO result = new PastWDTO();
		result.setLowestTemp(lowestTemp);
		result.setHighestTemp(highestTemp);
		result.setWeather(weather);

		return result;
	}
}
