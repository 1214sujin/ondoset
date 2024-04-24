package com.ondoset.dto.ootd;

import com.ondoset.common.Enum;
import com.ondoset.domain.Enum.TempRate;
import com.ondoset.domain.Enum.Weather;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherDTO {

	@Enum(enumClass = Weather.class)
	private String weather;
	@Enum(enumClass = TempRate.class)
	private String tempRate;
	private Long lastPage;
}
