package com.ondoset.dto.kma;

import com.ondoset.domain.Enum.Weather;
import com.ondoset.dto.clothes.FcstDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ForecastDTO {

	private Double now;
	private Double diff;
	private Double feel;
	private Weather weather;
	private Long min;
	private Long max;
	private List<FcstDTO> fcst;
}
