package com.ondoset.common;

import java.time.LocalTime;

public final class TimeConstants {

	private TimeConstants() {}	// 인스턴스화 방지

	public static final int SECS_OF_DAY = 86400;
	public static final int SECS_OFFSET = 32400;

	public static long calcDateTimestamp(long timestamp) {
		return ((timestamp + SECS_OFFSET) / SECS_OF_DAY) * SECS_OF_DAY - SECS_OFFSET;
	}

	/**
	 * @param localTime 사용자가 요청한 시각 값
	 * @param minute 시 단위로 변환할 때 기준이 되는 값
	 *               <br>
	 *               ex: minute=40이면, 3:39은 2시, 3:40은 3시 반환
	 * @return h (-1<=h<=23)
	 */
	public static int calcReqHour(LocalTime localTime, int minute) {

		// minute 값에 따라 시 단위를 반환
		if (localTime.getMinute() < minute)
			return localTime.getHour() - 1;	// -1이 나올 수 있음. 외부에서 처리
		else
			return localTime.getHour();
	}
}
