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

	public List<Long> getRecommend(Long memberId) {
		return Arrays.asList(19L, 20L, 25L, 23L);
	}

	public Satisfaction getSatisfaction(List<Clothes> clothesList) {
		return Satisfaction.GOOD;
	}

	public List<Long> getSimilarUser(Long memberId) {
		return Arrays.asList(2L, 3L);
	}
}
