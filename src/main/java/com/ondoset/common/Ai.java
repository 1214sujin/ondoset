package com.ondoset.common;

import com.ondoset.domain.Clothes;
import com.ondoset.domain.Enum.Satisfaction;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Log4j2
@Component
public class Ai {

	public List<List<List<Long>>> getRecommend(Long memberId) {
		//[[태그, 태그, 태그], [두께감, null, null]] * 3
		// -1: THIN, 0: NORMAL, 1: THICK
		List<List<Long>> rec1 = Arrays.asList(Arrays.asList(1L, 14L, 28L, 34L, 50L), Arrays.asList(null, null, null, null));
		List<List<Long>> rec2 = Arrays.asList(Arrays.asList(2L, 2L, 14L, 34L, 50L), Arrays.asList(null, null, null, null));
		List<List<Long>> rec3 = Arrays.asList(Arrays.asList(6L, 14L, 34L, 50L), Arrays.asList(-1L, 0L, null, null));
		return Arrays.asList(rec1, rec2, rec3);
	}

	public Satisfaction getSatisfaction(List<Clothes> clothesList) {
		return Satisfaction.GOOD;
	}

	public List<Long> getSimilarUser(Long memberId) {
		return Arrays.asList(2L, 3L);
	}

	// 사라질 수도 있음
	public List<Long> getSimilarDate(Long date) {
		return Arrays.asList(1713193200L, 1713279600L, 1713366000L, 1713452400L);
	}
}
