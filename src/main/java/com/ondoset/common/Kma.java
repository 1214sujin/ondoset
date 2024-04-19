package com.ondoset.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.Enum.Weather;
import com.ondoset.dto.clothes.FcstDTO;
import com.ondoset.dto.kma.DayWDTO;
import com.ondoset.dto.kma.ForecastDTO;
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
import java.util.*;

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
	private JsonArray getAPIRes(String reqUrl) {

		JsonObject result;
		try {
			URL url = new URL(reqUrl);
			HttpURLConnection con;
			BufferedReader in;
			String response;
			do {
				con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("Content-Type", "application/json");

				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				in.mark(500000);
				response = in.readLine();

				in.reset();
			} while (response.charAt(0) == '<');	// 기상청 API 서버 내부 에러가 발생하는 경우 재요청: <OpenAPI_ServiceResponse> ...
			in.close();
			con.disconnect();

			result = JsonParser.parseString(response).getAsJsonObject();
		}
		catch (IOException e) {
			throw new CustomException(ResponseCode.COM4000, "오류가 발생한 요청 API: "+reqUrl);
		}

		String kmaErrorCode = result.getAsJsonObject("response").getAsJsonObject("header").get("resultCode").toString().replace("\"", "");
		if (!kmaErrorCode.equals("00")) {
			throw new CustomException(ResponseCode.COM5000, "기상청 API 응답에 오류가 있습니다: " + kmaErrorCode + ", " + reqUrl);
		} else {
			return result.getAsJsonObject("response").getAsJsonObject("body").getAsJsonObject("items").getAsJsonArray("item");
		}
	}

	// 위도/경도를 관측 지점 정보로 변환
	private String getStn(Double lat, Double lon, String time) {

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

	/** 과거 날씨 조회 */
	public PastWDTO getPastW(Double lat, Double lon, Long departTime, Long arrivalTime) {

		// 날짜를 API 요청 형식으로 변환
		String departReqTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(departTime), ZoneId.of("Asia/Seoul")).format(kmaFormatter);
		String arrivalReqTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(arrivalTime), ZoneId.of("Asia/Seoul")).format(kmaFormatter);

		// 사용자 위치에 대한 stn 값 획득
		String stn = getStn(lat, lon, departReqTime);

		// 지상 관측 시간자료 API 요청
		JsonArray items = getAPIRes(String.format("https://apis.data.go.kr/1360000/AsosHourlyInfoService/getWthrDataList" +
				"?numOfRows=48&pageNo=1&dataType=JSON&dataCd=ASOS&dateCd=HR&startDt=%s&startHh=%s&endDt=%s&endHh=%s&stnIds=%s&serviceKey=%s",
				departReqTime.substring(0, 8), departReqTime.substring(8, 10), arrivalReqTime.substring(0, 8), arrivalReqTime.substring(8, 10), stn, serviceKey));

		int lowestTemp = 99;
		int highestTemp= -99;
		// 순서대로 RAINNY, SNOWY, PARTLY_CLOUDY, CLOUDY, SUNNY (우선순위순)
		int[] weatherCount = {0, 0, 0, 0, 0};

		for (JsonElement e : items.getAsJsonArray()) {
			String ta = e.getAsJsonObject().get("ta").toString().replace("\"", "");
			int tempAvg = Math.toIntExact(Math.round(Double.parseDouble(ta)));

			if (tempAvg < lowestTemp) {
				lowestTemp = tempAvg;
			}
			if (tempAvg > highestTemp) {
				highestTemp = tempAvg;
			}

			String rain = e.getAsJsonObject().get("rn").toString().replace("\"", "");
			String snow = e.getAsJsonObject().get("hr3Fhsc").toString().replace("\"", "");

			if (snow.equals("")) {
				if (rain.equals("")) {
					double cloud = Double.parseDouble(e.getAsJsonObject().get("dc10Tca").toString().replace("\"", ""));

					if (cloud <= 5)		weatherCount[4] += 1;
					else if(cloud <= 8)	weatherCount[2] += 1;
					else /* 흐림 */		weatherCount[3] += 1;
				} else /* 비 */		weatherCount[0] += 1;
			} else /* 눈 */		weatherCount[1] += 1;
		}

		// 날씨 구하기
		Weather weather = null;
		int max = -1;
		int maxIdx = -1;
		for (int i = 0; i < 5; i++) {
			if (weatherCount[i] > max) {
				max = weatherCount[i];
				maxIdx = i;
			}
		}
		switch (maxIdx) {
			case 0: if (weatherCount[0] == weatherCount[1]) weather = Weather.SLEET; else weather = Weather.RAINY; break;
			case 1: weather = Weather.SNOWY; break;
			case 2: weather = Weather.PARTLY_CLOUDY; break;
			case 3: if (weatherCount[3] == weatherCount[4]) weather = Weather.PARTLY_CLOUDY; else weather = Weather.CLOUDY; break;
			case 4: weather = Weather.SUNNY; break;
		}

		PastWDTO result = new PastWDTO();
		result.setLowestTemp(lowestTemp);
		result.setHighestTemp(highestTemp);
		result.setWeather(weather);

		return result;
	}

	// 위도/경도를 X/Y 격자로 변환
	private Map<String, String> getXY(Double lat, Double lon) {

		String reqUrl = String.format("https://apihub.kma.go.kr/api/typ01/cgi-bin/url/nph-dfs_xy_lonlat?lon=%f&lat=%f&help=0&authKey=%s", lon, lat, authKey);

		try {
			URL url = new URL(reqUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			// 설명 라인 버리기
			in.readLine();
			in.readLine();

			// 라인 파싱
			String inputLine = in.readLine();
			String[] tuple = inputLine.replaceAll(" +", "").split(",");

			String x = tuple[2];
			String y = tuple[3];
			in.close();
			con.disconnect();

			Map<String, String> result = new HashMap<>();
			result.put("x", x);
			result.put("y", y);

			return result;
		}
		catch (IOException e) {
			throw new CustomException(ResponseCode.COM4000, "오류가 발생한 요청 API: "+reqUrl);
		}
	}

	// 어제와의 기온 비교를 위해 과거 관측 값 획득
	private Double getLastW(Double lat, Double lon, Long date) {

		// 날짜를 API 요청 형식으로 변환
		String reqTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(date), ZoneId.of("Asia/Seoul")).format(kmaFormatter);

		// 사용자 위치에 대한 stn 값 획득
		String stn = getStn(lat, lon, reqTime);

		// 지상 관측 시간자료 API 요청
		JsonArray items = getAPIRes(String.format("https://apis.data.go.kr/1360000/AsosHourlyInfoService/getWthrDataList" +
				"?numOfRows=1&pageNo=1&dataType=JSON&dataCd=ASOS&dateCd=HR&startDt=%s&startHh=%s&endDt=%1$s&endHh=%2$s&stnIds=%s" +
				"&serviceKey=%s", reqTime.substring(0, 8), reqTime.substring(8, 10), stn, serviceKey));

		Double lastTemp = null;
		for (JsonElement e : items.getAsJsonArray()) {
			String ta = e.getAsJsonObject().get("ta").toString().replace("\"", "");
			lastTemp = Double.parseDouble(ta);
		}
		return lastTemp;
	}

	// 요청 날짜의 날씨 예보 획득
	private DayWDTO getDayW(String x, String y, int timeFromToday, String date) {

		long now = Instant.now().getEpochSecond();

		// 현재 시각을 기준으로 가장 최근의 API 요청 base_time
		Integer numOfRows = null;
		String time = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("Asia/Seoul")).format(kmaFormatter);
		int clock = (int) (now + 35400 - ((now + 32400) / 86400) * 86400) / 10800;
		switch (clock) {
			case 0 -> {
				clock = 8;
				numOfRows = 290 + 290 * timeFromToday;
				time = LocalDateTime.ofInstant(Instant.now().minusSeconds(86400), ZoneId.of("Asia/Seoul")).format(kmaFormatter);
			}
			case 1 -> numOfRows = 254 + 290 * timeFromToday;
			case 2 -> numOfRows = 217 + 290 * timeFromToday;
			case 3 -> numOfRows = 181 + 290 * timeFromToday;
			case 4 -> numOfRows = 145 + 290 * timeFromToday;
			case 5 -> numOfRows = 108 + 290 * timeFromToday;
			case 6 -> numOfRows = 72 + 290 * timeFromToday;
			case 7 -> numOfRows = 36 + 290 * timeFromToday;
		}

		JsonArray items = getAPIRes(String.format("https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0/getVilageFcst" +
				"?pageNo=1&numOfRows=%d&dataType=JSON&base_date=%s&base_time=%s&nx=%s&ny=%s" +
				"&authKey=%s", numOfRows, time.substring(0, 8), String.format("%02d00", clock*3-1), x, y, authKey));

		DayWDTO result = new DayWDTO();
		Map<Integer, Map<String, String>> dto = new HashMap<>();

		// 응답으로부터 예보 정보 획득
		for (JsonElement e : items.getAsJsonArray()) {

			String fcstDate = e.getAsJsonObject().get("fcstDate").toString().replace("\"", "");

			// 요청된 날짜와 같은 날짜에 대해서만 저장
			if (fcstDate.equals(date)) {

				String category = e.getAsJsonObject().get("category").toString().replace("\"", "");
				String fcstValue = e.getAsJsonObject().get("fcstValue").toString().replace("\"", "");
				int fcstTime = Integer.parseInt(e.getAsJsonObject().get("fcstTime").toString().replace("\"", "").substring(0, 2));

				if (!dto.containsKey(fcstTime)) {
					dto.put(fcstTime, new HashMap<>());
				}
				switch (category) {
					case "TMP" -> dto.get(fcstTime).put("tmp", fcstValue);
					case "POP" -> dto.get(fcstTime).put("pop", fcstValue);
					case "PTY" -> dto.get(fcstTime).put("pty", fcstValue);
					case "SKY" -> dto.get(fcstTime).put("sky", fcstValue);
					case "TMN" -> result.setMin(Double.valueOf(fcstValue).longValue());
					case "TMX" -> result.setMax(Double.valueOf(fcstValue).longValue());
				}
			}
		}

		List<FcstDTO> fcst = new ArrayList<>();

		for (Integer fcstTime : dto.keySet()) {

			int pty = Integer.parseInt(dto.get(fcstTime).get("pty"));
			Weather weather;

			if (pty == 1 | pty == 4) weather = Weather.RAINY;
			else if (pty == 2) weather = Weather.SLEET;
			else if (pty == 3)	weather = Weather.SNOWY;
			else {
				int sky = Integer.parseInt(dto.get(fcstTime).get("sky"));
				if (sky == 1)	weather = Weather.SUNNY;
				else if (sky == 3)	weather = Weather.PARTLY_CLOUDY;
				else if (sky == 4)	weather = Weather.CLOUDY;
				else throw new CustomException(ResponseCode.COM5000);
			}
			Long tmp = Long.valueOf(dto.get(fcstTime).get("tmp"));
			Integer pop = Integer.valueOf(dto.get(fcstTime).get("pop"));

			fcst.add(new FcstDTO(fcstTime, tmp, pop, weather));
		}

		result.setFcst(fcst);

		return result;
	}

	// 현재 날씨 획득
	private List<FcstDTO> getNowW(Integer x, Integer y) {

		// https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0/getVilageFcst
		// ?pageNo=1&numOfRows=%d&dataType=JSON&base_date=%s&base_time=%s&nx=%s&ny=%s
		// &authKey=%s
		long now = Instant.now().getEpochSecond();
	}

	/** 요청 날짜의 날씨 및 예보 조회 */
	public ForecastDTO getForecast(Double lat, Double lon, Long date) {

		// 오늘로부터 어느 시점에 대한 요청인지 확인 (0: 오늘, 1: 내일, 2: 모레)
		long now = Instant.now().getEpochSecond();
		int timeFromToday = (int) ((date - now) / 86400 + 1);

		// 날짜를 API 요청 형식으로 변환
		String reqNowTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(date), ZoneId.of("Asia/Seoul")).format(kmaFormatter);

		// 위도/경도를 X/Y 격자로 변환
		Map<String, String> xy = getXY(lat, lon);
		String x = xy.get("x");
		String y = xy.get("y");

		ForecastDTO res = new ForecastDTO();

		switch (timeFromToday) {
			case 0 -> {
				// 초단기예보 API 호출 및 현재 기온/강수형태/하늘상태 획득, 풍속/습도 획득하여 체감온도 계산

				// 현재 날씨를 획득
				res.setNow();
				res.setDiff();
				res.setFeel();
				res.setWeather();
			}
			case 1, 2 -> {
				// 최저/최고 기온 및 하루 예보 획득

				res.setDiff(null);
				res.setFeel(null);

				// 단기예보 API 호출 및 내일or모레에 대한 기온/하늘상태 획득

				// 요청 날짜의 9-9 평균 기온 및 평균 하늘상태를 획득
				res.setNow();
				res.setWeather();
			}
			default -> throw new CustomException(ResponseCode.COM4000);
		}

		// 최저/최고 기온 및 하루 예보 획득
		DayWDTO fcst= getDayW(x, y, timeFromToday, reqNowTime.substring(0, 8));
		res.setMin(fcst.getMin());
		res.setMax(fcst.getMax());
		res.setFcst(fcst.getFcst());

		return res;
	}
}
