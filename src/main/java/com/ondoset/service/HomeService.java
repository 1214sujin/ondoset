package com.ondoset.service;

import com.ondoset.common.Ai;
import com.ondoset.common.TimeConstants;
import com.ondoset.common.Kma;
import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.domain.*;
import com.ondoset.domain.Enum.Category;
import com.ondoset.domain.Enum.Thickness;
import com.ondoset.domain.Enum.Weather;
import com.ondoset.domain.Tag;
import com.ondoset.dto.clothes.*;
import com.ondoset.dto.kma.ForecastDTO;
import com.ondoset.dto.kma.UltraSrtNcstDTO;
import com.ondoset.repository.CoordiRepository;
import com.ondoset.repository.MemberRepository;
import com.ondoset.repository.OOTDRepository;
import com.ondoset.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Log4j2
@RequiredArgsConstructor
@Service
@Transactional
public class HomeService {

	private final MemberRepository memberRepository;
	private final CoordiRepository coordiRepository;
	private final OOTDRepository ootdRepository;
	private final TagRepository tagRepository;
	private final Kma kma;
	private final Ai ai;

	private final Executor threadPoolTaskExecutor;

	public HomeDTO.res getHome(HomeDTO.req req) {

		// 현재 사용자 조회
		Member member = memberRepository.findByName(SecurityContextHolder.getContext().getAuthentication().getName());

		// req 분해
		LocalDate date = LocalDate.ofInstant(Instant.ofEpochSecond(req.getDate()), ZoneId.of("Asia/Seoul"));
		Double lat = req.getLat();
		Double lon = req.getLon();
		// 지금 시각
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

		// 응답 정의 { 날씨, 계획, 기록, 추천, OOTD }
		HomeDTO.res res = new HomeDTO.res();

		// 오늘로부터 어느 시점에 대한 요청인지 확인 (0: 오늘, 1: 내일, 2: 모레)
		int daysFromToday = (int) (date.toEpochDay() - now.toLocalDate().toEpochDay());
		log.debug("daysFromToday: {}", daysFromToday);
		// 처리할 수 없는 날짜에 대한 요청인 경우 잘못된 요청으로 판단
		if (daysFromToday < 0 || daysFromToday > 2) throw new CustomException(ResponseCode.COM4000);

		// 위경도_xy 전환 (kma)
		CompletableFuture<Map<String, String>> xyFuture = kma.getXY(lat, lon)
				.exceptionally(e -> {
					log.debug("kma.getXY 에서 오류 발생");
					throw new CustomException(ResponseCode.COM5000);
				});

		// 위경도_xy 전환 (kma) -> 단기예보 조회 (kma) -> 단기예보 파싱(parseForecast)
		CompletableFuture<Map<String, Object>> forecastMapFuture = xyFuture
				.thenComposeAsync(xy -> {

					// 단기예보 요청 인자 생성 - base date
					LocalDate baseLocalDate = now.toLocalDate();

					// 02시가 지나지 않았다면, 오늘의 예보가 아직 올라오지 않았으므로 어제 예보된 것을 조회
					if (now.getHour() <= 2)
						baseLocalDate = baseLocalDate.minusDays(1);

					// 단기예보 요청 인자 생성 - base time (현재 시각을 기준으로 최신 결과를 받기 위함)

					// now에서 10분을 뺀 시각(a)을 2|5|8|11|14|17|20|23을 기준으로 나누어야 함 (base_time)
					// => a에서 1시간을 더한 시각은 0~3~6~9~12~15~18~21~24를 기준으로 범위가 나누어짐 (b)
					// => b를 3시간으로 나누면 총 8개의 범위(0-7)로 나타낼 수 있음
					int clock = (TimeConstants.calcReqHour(now.toLocalTime(), 10) + 1) / 3;	// clock * 3 - 1 => base_time
					// numOfRows에 따라 응답 시간이 달라지기 때문에, 경우에 따라 최소한의 rows만 요청한다.
					int numOfRows;
					switch ((clock + 1) / 3) {
						case 0 -> {											// [00:10, 02:10)
							clock = 8;
							numOfRows = 290 + 290 * daysFromToday;
						}
						case 1 -> numOfRows = 254 + 290 * daysFromToday;	// [02:10, 05:10)
						case 2 -> numOfRows = 217 + 290 * daysFromToday;	// [05:10, 08:10)
						case 3 -> numOfRows = 181 + 290 * daysFromToday;	// [08:10, 11:10)
						case 4 -> numOfRows = 145 + 290 * daysFromToday;	// [11:10, 14:10)
						case 5 -> numOfRows = 108 + 290 * daysFromToday;	// [14:10, 17:10)
						case 6 -> numOfRows = 72 + 290 * daysFromToday;		// [17:10, 20:10)
						case 7 -> numOfRows = 36 + 290 * daysFromToday;		// [20:10, 23:10)
						case 8 -> numOfRows = 290 + 290 * daysFromToday;	// [23:10, 00:10)
						default -> throw new CustomException(ResponseCode.COM4000);
					}

					return kma.getVilageFcst(numOfRows, baseLocalDate, clock*3-1, xy);
				}, threadPoolTaskExecutor)
				.exceptionally(e -> {
					log.debug("kma.getVilageFcst 에서 오류 발생");
					throw new CustomException(ResponseCode.COM5000);
				})
				.thenApply(vilageFcstList -> {

					// 23시가 지났다면, 오늘 예보는 남은 것이 없으므로 내일 것을 조회
					LocalDate localDate = (now.getHour() == 23)
							? date.plusDays(1)
							: date;

					if (daysFromToday == 0)
						return kma.parseTodayForecast(vilageFcstList, localDate);
					else
						return kma.parseLaterForecast(vilageFcstList, localDate);
				})
				.exceptionally(e -> {
					log.debug("kma.parse_Forecast 에서 오류 발생");
					throw new CustomException(ResponseCode.COM5000);
				});

		// 위경도_xy 전환 (kma) -> 오늘과 날씨가 유사한 날짜 (ai)
		CompletableFuture<List<Long>> similarDateFuture = xyFuture
				.thenApplyAsync(xy -> {
					long nowTimestamp = now.toEpochSecond(ZoneOffset.of("+9"));
					return ai.getSimilarDate(member, xy, nowTimestamp, daysFromToday);
				}, threadPoolTaskExecutor)
				.exceptionally(e -> {
					log.debug("ai.getSimilarDate 에서 오류 발생");
					throw new CustomException(ResponseCode.COM5000);
				});

		CompletableFuture<Void> settingForecast = forecastMapFuture.thenAcceptBothAsync(xyFuture, (forecastMap, xy) -> {

			ForecastDTO forecast = (ForecastDTO) forecastMap.get("forecast");

			// 오늘에 대한 요청인 경우 날씨 현황을 받아야 한다
			if (daysFromToday == 0) {

				// 위경도_stn 전환 (kma) -> 과거 날씨 기록 조회 (kma) -> 어제의 지금 시점 기온 획득
				CompletableFuture<Double> lastTempFuture = kma.getStn(lat, lon, now)
						.thenCompose(stn -> kma.getWthrData(now.minusDays(1), stn))
						.thenApply(wthrDataList -> Double.parseDouble(wthrDataList.get(0).getTa()))
						.exceptionally(e -> -99.0);

				// 초단기실황 요청 인자 생성 (kma) -> 현재 날씨 조회
				LocalDate localDate;
				int clock = TimeConstants.calcReqHour(now.toLocalTime(), 40);
				if (clock < 0) {
					clock = 23;
					localDate = now.minusDays(1).toLocalDate();
				} else {
					localDate = now.toLocalDate();
				}

				kma.getUltraNcst(localDate, clock, xy)
						.exceptionally(e -> {
							log.debug("kma.getUltraNcst 에서 오류 발생");
							throw new CustomException(ResponseCode.COM5000);
						})
						.thenApply(ultraNcstList -> {
							Double tmp = null; Integer pty = null; Double wsd = null; Integer reh = null;

							for (UltraSrtNcstDTO n : ultraNcstList) {

								switch (n.getCategory()) {
									case "T1H" -> tmp = Double.parseDouble(n.getObsrValue());
									case "PTY" -> pty = Integer.parseInt(n.getObsrValue());
									case "WSD" -> wsd = Double.parseDouble(n.getObsrValue());
									case "REH" -> reh = Integer.parseInt(n.getObsrValue());
								}
							}
							if (tmp == null || pty == null || wsd == null || reh == null)
								throw new CustomException(ResponseCode.COM5000, "현재 날씨 정보에 빈 정보가 포함되어 있습니다.");

							// 현재 기온, 전날 대비, 체감 온도
							forecast.setNow(tmp);
							int month = date.getMonthValue();
							if (month >= 5 && month <= 9)
								forecast.setFeel(getSummerFeel(tmp, reh));
							else
								forecast.setFeel(getWinterFeel(tmp, wsd));

							// 하늘 상태
							Weather weather = switch (pty) {
								case 1, 5 -> Weather.RAINY;
								case 2, 6 -> Weather.SLEET;
								case 3, 7 -> Weather.SNOWY;
								default -> switch ((int) forecastMap.get("nowSky")) {
									case 1 -> Weather.SUNNY;
									case 3 -> Weather.PARTLY_CLOUDY;
									case 4 -> Weather.CLOUDY;
									default -> throw new CustomException(ResponseCode.COM5000);
								};
							};
							forecast.setWeather(weather);

							return tmp;
						})
						.exceptionally(e -> {
							log.debug("Ncst 파싱 과정에서 오류 발생");
							throw new CustomException(ResponseCode.COM5000);
						})
						.thenAcceptBoth(lastTempFuture, (tmp, lastTemp) -> {

							if (lastTemp.equals(-99.0)) forecast.setDiff(null);
							else forecast.setDiff(Math.round(tmp - lastTemp) * 10 / 10.0);
						});
			}
			res.setForecast(forecast);

		}, threadPoolTaskExecutor);

		// 코디 계획 - 응답[1]
		Optional<Coordi> planCoordiOptional = coordiRepository.findByConsistings_Clothes_MemberAndDate(member,
				date.atStartOfDay().toEpochSecond(ZoneOffset.of("+9")));
		planCoordiOptional.ifPresent(coordi -> res.setPlan(getPlan(coordi)));

		// 유사 유저 리스트 획득 (ai) -> 유사 유저의 최신 OOTD
		// OOTD - 응답[4]
		res.setOotd(getOotdPreview(ai.getSimilarUser(member.getId())));

		settingForecast.join();

		// DB 접근을 위한 동기 작업

		double tempAvg = (double) forecastMapFuture.join().get("tempAvg");
		member.setRecentReqTemp(tempAvg);
		memberRepository.save(member);

		List<Long> dateList = similarDateFuture.join();

		// 코디 기록 - 응답[2]
		res.setRecord(getRecord(member, dateList, tempAvg));
		log.debug("record[0][0]: {}", res.getRecord().get(0).getClothesList().get(0));

		// 코디 추천 - 응답[3]
		res.setRecommend(getRecommend(ai.getRecommend(tempAvg, member.getId())));
		log.debug("recommend[0][0]: {}", res.getRecommend().get(0).get(0).getFullTag());

		return res;
	}

	private Double getSummerFeel(Double ta, Integer rh) {

		double tw = ta * Math.atan(0.151977 * Math.pow(rh + 8.313659, 0.5)) + Math.atan(ta + rh)
				- Math.atan(rh - 1.67633) + 0.00391838 * Math.pow(rh, 1.5) * Math.atan(0.023101 * rh) - 4.686035;

		return Math.round((-0.2442 + 0.55399 * tw + 0.45535 * ta - 0.0022 * Math.pow(tw, 2) + 0.00278 * tw * ta + 3.0) * 10) / 10.0;
	}

	private Double getWinterFeel(Double ta, Double wsd) {

		if (ta > 10 || wsd < 1.3) return ta;

		double v = Math.pow(wsd * 18.0/5.0, 0.16);
		return Math.round((13.12 + 0.6215 * ta - 11.37 * v + 0.3965 * ta * v) * 10) / 10.0;
	}

	// 계획
	public List<PlanDTO> getPlan(Coordi planCoordi) {

		List<PlanDTO> plan = new ArrayList<>();
		for (Consisting cs : planCoordi.getConsistings()) {

			Clothes ct = cs.getClothes();

			PlanDTO p = new PlanDTO();
			p.setClothesId(ct.getId());
			p.setName(ct.getName());
			p.setImageURL(ct.getImageURL());
			p.setCategory(ct.getTag().getCategory());
			p.setTag(ct.getTag().getName());
			p.setTagId(ct.getTag().getId());
			p.setThickness(ct.getThickness());

			plan.add(p);
		}
		return plan;
	}

	// 기록
	public List<RecordDTO> getRecord(Member member, List<Long> dateList, Double tempAvg) {

		List<RecordDTO> record = new ArrayList<>();
		for (Long date : dateList) {

			Optional<Coordi> coordiOptional = coordiRepository.findByConsistings_Clothes_MemberAndDate(member, date - 32400);
			if (coordiOptional.isPresent() && coordiOptional.get().getDepartTime()!=null) {

				// 평균 기온을 비교하여 적절한 응답인지 확인
				Coordi coordi = coordiOptional.get();
				try{
					log.debug("coordi: {}", coordi);
					log.debug("coordi[0]: {}", coordi.getConsistings().get(0).getClothes().getName());
				} catch (Exception e) {
					log.debug(e.getMessage());
				}
				double diff = (Double.valueOf(coordi.getHighestTemp()) + Double.valueOf(coordi.getLowestTemp())) / 2 - tempAvg;
				if (diff > 5 || diff < -5) {
					log.debug("plan of {} is not offered to member {} because diff is {}", date, member.getId(), diff);
					continue;
				}

				List<Consisting> consistingList = coordi.getConsistings();

				RecordDTO r = new RecordDTO();
				r.setDate(date);

				List<ClothesDTO> clothesList = new ArrayList<>();
				for (Consisting cs : consistingList) {

					Clothes c = cs.getClothes();

					ClothesDTO clothes = new ClothesDTO();
					clothes.setClothesId(c.getId());
					clothes.setName(c.getName());
					clothes.setImageURL(c.getImageURL());
					clothes.setCategory(c.getTag().getCategory());
					clothes.setTag(c.getTag().getName());
					clothes.setTagId(c.getTag().getId());
					clothes.setThickness(c.getThickness());

					clothesList.add(clothes);
				}
				r.setClothesList(clothesList);

				record.add(r);
			}
		}
		return record;
	}

	// 추천
	@Transactional
	public List<List<RecommendDTO>> getRecommend(List<List<List<Long>>> tagRecommendList) {

		List<List<RecommendDTO>> recommend = new ArrayList<>();
		for (List<List<Long>> tagRecommend : tagRecommendList) {

			List<RecommendDTO> recommendElement = new ArrayList<>();
			for (int i = 0; i < tagRecommend.get(0).size(); i++) {

				RecommendDTO r = new RecommendDTO();

				// 중복 태그는 한 번만 조회되는 문제 때문에 in으로 한 번에 조회할 수 없음
				Tag tag = tagRepository.findById(tagRecommend.get(0).get(i)).get();
				String tagName = tag.getName();

				r.setTag(tagName);
				r.setTagId(tag.getId());
				Category category = tag.getCategory();
				r.setCategory(category);

				if (Arrays.asList(Category.SHOE, Category.ACC).contains(category)) {
					r.setThickness(null);
					r.setFullTag(tagName);
				} else {
					Long thicknessCode = tagRecommend.get(1).get(i);
					Thickness thickness = switch (thicknessCode.intValue()) {
						case -1 -> Thickness.THIN;
						case 1 ->  Thickness.THICK;
						default -> Thickness.NORMAL;
					};
					r.setThickness(thickness);
					r.setFullTag(String.join(" ", thickness.getName(), tagName));
				}
				recommendElement.add(r);
			}
			recommend.add(recommendElement);
		}
		return recommend;
	}

	// OOTD
	public List<OotdShortDTO> getOotdPreview(List<Long> memberIdList) {

		List<OOTD> ootdList;
		if (memberIdList.isEmpty()) {
			// 전체 ootd를 최신 3개만 획득
			ootdList = ootdRepository.findTop3ByOrderByIdDesc();
		} else {
			// 해당 memberId 목록에 속하는 ootd를 최신 3개만 획득
			ootdList = ootdRepository.findTop3ByMember_IdInOrderByIdDesc(memberIdList);
		}

		List<OotdShortDTO> ootdPreview = new ArrayList<>();
		for (OOTD o : ootdList) {

			OotdShortDTO ootd = new OotdShortDTO();
			ootd.setImageURL(o.getImageURL());
			ootd.setDate(((o.getDepartTime()+32400)/86400)*86400-32400);
			ootdPreview.add(ootd);
		}
		return ootdPreview;
	}
}
