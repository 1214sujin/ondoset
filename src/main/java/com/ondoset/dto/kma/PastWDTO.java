package com.ondoset.dto.kma;

import com.ondoset.domain.Enum.Weather;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PastWDTO {

	private Integer lowestTemp;
	private Integer highestTemp;
	private Weather weather;
}
