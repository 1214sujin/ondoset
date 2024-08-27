package com.ondoset.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.Enum.Weather;
import com.ondoset.dto.clothes.FcstDTO;
import com.ondoset.dto.kma.*;
import com.ondoset.dto.ootd.WeatherPreviewDTO;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
public class Kma {

	private final WebClient webClientAuth;
	private final WebClient webClientService;
	private final ObjectMapper objectMapper;
	@Value("${com.ondoset.kma.auth_key}")
	private String authKey;
	@Value("${com.ondoset.data.service_key}")
	private String serviceKey;

	private final DateTimeFormatter kmaDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(TimeZone.getDefault().toZoneId());
	private final DateTimeFormatter kmaDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(TimeZone.getDefault().toZoneId());

	@Autowired
	public Kma(@Qualifier("webClientAuth") WebClient webClientAuth,
					 @Qualifier("webClientService") WebClient webClientService,
					 ObjectMapper objectMapper) {
		this.webClientAuth = webClientAuth;
		this.webClientService = webClientService;
		this.objectMapper = objectMapper;
	}

	private <T> Mono<List<T>> getServiceRes(String resUri, TypeReference<List<T>> typeReference) {

		return webClientService.get()
				.uri(resUri)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<KmaData<T>>() {})
				.flatMap(kmaData -> {

					// "response": {"body": {"items": {"item": [] } } } 구조에서 item만 추출하여 반환
					try {
						List<T> item = kmaData.getResponse().getBody().getItems().getItem();
						item = objectMapper.readValue(objectMapper.writeValueAsString(item), typeReference);
						return Mono.just(item);
					} catch (JsonProcessingException e) {
						return Mono.error(new RuntimeException("JsonProcessingException: item 객체 추출 실패"));
					}
				})
				.retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100))
						.doBeforeRetry(retrySignal -> log.info("Retrying...")))
				.doOnError(e -> {
					log.error("오류가 발생한 요청 API: https://apis.data.go.kr/1360000{}", resUri);
					throw (CustomException) e;
				});
	}

	/** 위경도를 xy 좌표로 변환
	 * x = xy.get("x"); */
	@Async
	public CompletableFuture<Map<String, String>> getXY(Double lat, Double lon) {

		String reqUri = String.format("/cgi-bin/url/nph-dfs_xy_lonlat" +
				"?lon=%f&lat=%f&help=0&authKey=%s", lon, lat, authKey);

		return webClientAuth.get()
				.uri(reqUri)
				.retrieve()
				.bodyToMono(String.class)
				.map(string -> {

					String[] input = string.split("\n");

					String[] tuple = input[2].replaceAll(" +", "").split(",");

					String x = tuple[2];
					String y = tuple[3];

					Map<String, String> result = new HashMap<>();
					result.put("x", x);
					result.put("y", y);

					return result;
				})
				.retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100))
						.doBeforeRetry(retrySignal -> log.info("Retrying...")))
				.doOnError(e -> log.error("오류가 발생한 요청 API: https://apihub.kma.go.kr/api/typ01{}", reqUri))
				.toFuture();
	}

	/** 위경도를 관측 지점 아이디(stn)으로 변환 */
	@Async
	public CompletableFuture<String> getStn(Double lat, Double lon, LocalDateTime reqTime) {

		String formattedReqTime = reqTime.format(kmaDateTimeFormatter);
		String reqUri = String.format("/url/stn_inf.php?inf=SFC&tm=%s&authKey=%s", formattedReqTime, authKey);

		return webClientAuth.get()
				.uri(reqUri)
				.retrieve()
				.bodyToMono(String.class)
				.map(string -> {

					String[] input = string.split("\n");

					String stn = "";
					double min_dist = 999.9;

					for (int i = 3; i < input.length; i++) {

						String inputLine = input[i];

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

					return stn;
				})
				.retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100))
						.doBeforeRetry(retrySignal -> log.info("Retrying...")))
				.doOnError(e -> log.error("오류가 발생한 요청 API: https://apihub.kma.go.kr/api/typ01{}", reqUri))
				.toFuture();
	}

	/** 날씨 예보 조회 (단기예보) */
	@Async
	public CompletableFuture<List<VilageFcstDTO>> getVilageFcst(Integer numOfRows,
														  LocalDate baseLocalDate,
														  Integer baseTime,
														  Map<String, String> xy) {

		String baseDate = baseLocalDate.format(kmaDateFormatter);
		String vilageFcstUri = String.format("/VilageFcstInfoService_2.0/getVilageFcst" +
				"?pageNo=1&numOfRows=%d&dataType=JSON&base_date=%s&base_time=%s&nx=%s&ny=%s" +
				"&serviceKey=%s", numOfRows, baseDate, String.format("%02d00", baseTime), xy.get("x"), xy.get("y"), serviceKey);

		return getServiceRes(vilageFcstUri, new TypeReference<List<VilageFcstDTO>>() {}).toFuture();
	}

	/** 과거 날씨 조회 (ASOS시간) */
	@Async
	public CompletableFuture<List<WthrDataDTO>> getWthrData(LocalDateTime departTime, LocalDateTime arrivalTime, String stn) {

		String formattedDepartTime = departTime.format(kmaDateTimeFormatter);
		String formattedArrivalTime = arrivalTime.format(kmaDateTimeFormatter);

		String wthrDataListUri = String.format("/AsosHourlyInfoService/getWthrDataList?numOfRows=48&pageNo=1" +
				"&dataType=JSON&dataCd=ASOS&dateCd=HR&startDt=%s&startHh=%s&endDt=%s&endHh=%s&stnIds=%s&serviceKey=%s",
				formattedDepartTime.substring(0, 8), formattedDepartTime.substring(8, 10),
				formattedArrivalTime.substring(0, 8), formattedArrivalTime.substring(8, 10), stn, serviceKey);

		return getServiceRes(wthrDataListUri, new TypeReference<List<WthrDataDTO>>() {}).toFuture();
	}
	@Async
	public CompletableFuture<List<WthrDataDTO>> getWthrData(LocalDateTime reqTime, String stn) {
		return getWthrData(reqTime, reqTime, stn);
	}

	/** 날씨 현황 조회 (초단기실황) */
	@Async
	public CompletableFuture<List<UltraSrtNcstDTO>> getUltraNcst(LocalDate localDate,
																 Integer baseTime,
																 Map<String, String> xy) {

		String baseDate = localDate.format(kmaDateFormatter);
		String ultraNcstUri = String.format("/VilageFcstInfoService_2.0/getUltraSrtNcst" +
				"?pageNo=1&numOfRows=8&dataType=JSON&base_date=%s&base_time=%s&nx=%s&ny=%s&serviceKey=%s",
				baseDate, String.format("%02d00", baseTime), xy.get("x"), xy.get("y"), serviceKey);

		return getServiceRes(ultraNcstUri, new TypeReference<List<UltraSrtNcstDTO>>() {}).toFuture();
	}

	private Double getSummerFeel(Double ta, Double rh) {

		double tw = ta * Math.atan(0.151977 * Math.pow(rh + 8.313659, 0.5)) + Math.atan(ta + rh)
				- Math.atan(rh - 1.67633) + 0.00391838 * Math.pow(rh, 1.5) * Math.atan(0.023101 * rh) - 4.686035;

		return Math.round((-0.2442 + 0.55399 * tw + 0.45535 * ta - 0.0022 * Math.pow(tw, 2) + 0.00278 * tw * ta + 3.0) * 10) / 10.0;
	}

	private Double getWinterFeel(Double ta, Double wsd) {

		if (ta > 10 || wsd < 1.3) return ta;

		double v = Math.pow(wsd * 18.0/5.0, 0.16);
		return Math.round((13.12 + 0.6215 * ta - 11.37 * v + 0.3965 * ta * v) * 10) / 10.0;
	}

	@Getter
	private static class KmaData<T> {

		@Getter
		private static class Header {
			private String resultCode;
			private String resultMsg;
		}

		@Getter
		private static class Body<T> {
			private String dataType;
			private Items<T> items;
			private Integer pageNo;
			private Integer numOfRows;
			private Integer totalCount;
		}

		@Getter
		private static class Items<T> {
			private List<T> item;
		}

		@Getter
		private static class Response<T> {
			private Header header;
			private Body<T> body;
		}

		private Response<T> response;
	}

	//////////////////////// 편의 메소드 ////////////////////////

	public Map<String, Object> parseTodayForecast(List<VilageFcstDTO> items, LocalDate localDate) {

		String fcstDate = localDate.format(kmaDateFormatter);

		ForecastDTO result = new ForecastDTO();
		Map<Integer, Map<String, Double>> fcstRes = new HashMap<>();

		result.setFcst(new ArrayList<>());

		for (VilageFcstDTO f : items) {

			// **요청된 날짜**에 대한 예보가 아닌 경우 pass
			if (!f.getFcstDate().equals(fcstDate)) continue;

			int fcstTime = Integer.parseInt(f.getFcstTime().substring(0, 2));

			if (!fcstRes.containsKey(fcstTime))
				fcstRes.put(fcstTime, new HashMap<>());
			switch (f.getCategory()) {
				case "TMP" -> fcstRes.get(fcstTime).put("tmp", Double.parseDouble(f.getFcstValue()));
				case "POP" -> fcstRes.get(fcstTime).put("pop", Double.parseDouble(f.getFcstValue()));
				case "PTY" -> fcstRes.get(fcstTime).put("pty", Double.parseDouble(f.getFcstValue()));
				case "SKY" -> fcstRes.get(fcstTime).put("sky", Double.parseDouble(f.getFcstValue()));
				case "TMN" -> result.setMin(Double.valueOf(f.getFcstValue()).longValue());
				case "TMX" -> result.setMax(Double.valueOf(f.getFcstValue()).longValue());
			}
		}

		double tmpSum = 0.0;
		Integer minFcstTime = 24;
		for (Integer fcstTime : fcstRes.keySet()) {

			Weather weather = switch (fcstRes.get(fcstTime).get("pty").intValue()) {
				case 1, 4 -> Weather.RAINY;
				case 2 -> Weather.SLEET;
				case 3 -> Weather.SNOWY;
				default -> switch (fcstRes.get(fcstTime).get("sky").intValue()) {
					case 1 -> Weather.SUNNY;
					case 3 -> Weather.PARTLY_CLOUDY;
					case 4 -> Weather.CLOUDY;
					default -> throw new CustomException(ResponseCode.COM5000);
				};
			};
			long tmp = fcstRes.get(fcstTime).get("tmp").longValue();
			tmpSum += tmp;
			int pop = fcstRes.get(fcstTime).get("pop").intValue();

			result.getFcst().add(new FcstDTO(fcstTime, tmp, pop, weather));

			if (fcstTime < minFcstTime) minFcstTime = fcstTime;
		}

		Double tmpAvg = tmpSum / fcstRes.keySet().size();
		Map<String, Object> res = new HashMap<>();
		res.put("forecast", result);
		res.put("tempAvg", tmpAvg);
		res.put("nowSky", fcstRes.get(minFcstTime).get("sky").intValue());

		return res;
	}

	public Map<String, Object> parseLaterForecast(List<VilageFcstDTO> items, LocalDate localDate) {

		String fcstDate = localDate.format(kmaDateFormatter);

		ForecastDTO result = new ForecastDTO();
		Map<Integer, Map<String, Double>> fcstRes = new HashMap<>();

		result.setFcst(new ArrayList<>());

		for (VilageFcstDTO f : items) {

			// **요청된 날짜**에 대한 예보가 아닌 경우 pass
			if (!f.getFcstDate().equals(fcstDate)) continue;

			int fcstTime = Integer.parseInt(f.getFcstTime().substring(0, 2));

			if (!fcstRes.containsKey(fcstTime))
				fcstRes.put(fcstTime, new HashMap<>());
			switch (f.getCategory()) {
				case "TMP" -> fcstRes.get(fcstTime).put("tmp", Double.parseDouble(f.getFcstValue()));
				case "POP" -> fcstRes.get(fcstTime).put("pop", Double.parseDouble(f.getFcstValue()));
				case "PTY" -> fcstRes.get(fcstTime).put("pty", Double.parseDouble(f.getFcstValue()));
				case "SKY" -> fcstRes.get(fcstTime).put("sky", Double.parseDouble(f.getFcstValue()));
				case "REH" -> fcstRes.get(fcstTime).put("reh", Double.parseDouble(f.getFcstValue()));
				case "WSD" -> fcstRes.get(fcstTime).put("wsd", Double.parseDouble(f.getFcstValue()));
				case "TMN" -> result.setMin(Double.valueOf(f.getFcstValue()).longValue());
				case "TMX" -> result.setMax(Double.valueOf(f.getFcstValue()).longValue());
			}
		}

		int month = Integer.parseInt(fcstDate.substring(4, 6));
		// 순서대로 SLEET, RAINNY, SNOWY, PARTLY_CLOUDY, CLOUDY, SUNNY
		int[] weather9to9 = {0, 0, 0, 0, 0, 0};
		double tmp9to9 = 0.0;
		double feel9to9 = 0.0;

		double tmpSum = 0.0;
		for (Integer fcstTime : fcstRes.keySet()) {

			Weather weather = switch (fcstRes.get(fcstTime).get("pty").intValue()) {
				case 1, 4 -> Weather.RAINY;
				case 2 -> Weather.SLEET;
				case 3 -> Weather.SNOWY;
				default -> switch (fcstRes.get(fcstTime).get("sky").intValue()) {
					case 1 -> Weather.SUNNY;
					case 3 -> Weather.PARTLY_CLOUDY;
					case 4 -> Weather.CLOUDY;
					default -> throw new CustomException(ResponseCode.COM5000);
				};
			};
			long tmp = fcstRes.get(fcstTime).get("tmp").longValue();
			tmpSum += tmp;
			int pop = fcstRes.get(fcstTime).get("pop").intValue();

			result.getFcst().add(new FcstDTO(fcstTime, tmp, pop, weather));

			if (fcstTime >= 9 && fcstTime <= 21) {
				switch (weather) {
					case SLEET -> weather9to9[0] += 1;
					case SNOWY -> weather9to9[1] += 1;
					case RAINY -> weather9to9[2] += 1;
					case PARTLY_CLOUDY -> weather9to9[3] += 1;
					case CLOUDY -> weather9to9[4] += 1;
					case SUNNY -> weather9to9[5] += 1;
				}
				tmp9to9 += tmp;
				if (month >= 5 && month <= 9) {
					double reh = fcstRes.get(fcstTime).get("reh");
					feel9to9 += getSummerFeel((double) tmp, reh);
				}
				else {
					double wsd = fcstRes.get(fcstTime).get("wsd");
					feel9to9 += getWinterFeel((double) tmp, wsd);
				}
			}
		}

		// 9to9 평균 기온 및 체감온도
		double tmp9to9Avg = (Math.round(tmp9to9 / 13 * 10)) / 10.0;
		result.setNow(tmp9to9Avg);
		result.setFeel((Math.round(feel9to9 / 13 * 10)) / 10.0);

		// 9to9 평균 하늘상태
		Weather weather9to9Avg = null;
		int max = -1;
		int maxIdx = -1;
		for (int i = 0; i < 6; i++) {
			if (weather9to9[i] > max) {
				max = weather9to9[i];
				maxIdx = i;
			}
		}
		switch (maxIdx) {
			case 0: weather9to9Avg = Weather.SLEET; break;
			case 1: if (weather9to9[0] == weather9to9[1]) weather9to9Avg = Weather.SLEET; else weather9to9Avg = Weather.RAINY; break;
			case 2: weather9to9Avg = Weather.SNOWY; break;
			case 3: weather9to9Avg = Weather.PARTLY_CLOUDY; break;
			case 4: if (weather9to9[3] == weather9to9[4]) weather9to9Avg = Weather.PARTLY_CLOUDY; else weather9to9Avg = Weather.CLOUDY; break;
			case 5: weather9to9Avg = Weather.SUNNY; break;
		}
		result.setWeather(weather9to9Avg);

		Double tmpAvg = tmpSum / fcstRes.keySet().size();
		Map<String, Object> res = new HashMap<>();
		res.put("forecast", result);
		res.put("tempAvg", tmpAvg);

		return res;
	}

	public Boolean canGetWthrData(LocalDateTime now, LocalDateTime reqDatetime) {
		long daysFromToday = now.toLocalDate().toEpochDay() - reqDatetime.toLocalDate().toEpochDay();
		return daysFromToday >= 0 && (daysFromToday != 0 || now.getHour() >= 11);
	}

	public WeatherPreviewDTO.res getWeatherPreviewFrom(List<WthrDataDTO> items) {

		int lowestTemp = 99;
		int highestTemp= -99;
		// 순서대로 RAINNY, SNOWY, PARTLY_CLOUDY, CLOUDY, SUNNY (우선순위순)
		int[] weatherCount = {0, 0, 0, 0, 0};

		for (WthrDataDTO e : items) {
			int ta = Math.toIntExact(Math.round(Double.parseDouble(e.getTa())));

			if (ta < lowestTemp) {
				lowestTemp = ta;
			}
			if (ta > highestTemp) {
				highestTemp = ta;
			}

			if (e.getHr3Fhsc().equals("")) {
				if (e.getRn().equals("")) {
					double cloud = Double.parseDouble(e.getDc10LmcsCa());

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

		WeatherPreviewDTO.res result = new WeatherPreviewDTO.res();
		result.setLowestTemp(lowestTemp);
		result.setHighestTemp(highestTemp);
		result.setWeather(weather);

		return result;
	}
}
