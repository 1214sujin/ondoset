package com.ondoset.dto.ootd;

import com.ondoset.domain.Enum.Weather;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class WeatherPreviewDTO {

	@Getter
	@Setter
	public static class req {

		@NotNull
		private Double lat;
		@NotNull
		private Double lon;
		@NotNull
		private Long departTime;
		@NotNull
		private Long arrivalTime;
	}

	@Getter
	@Setter
	public static class res {

		private Integer lowestTemp;
		private Integer highestTemp;
		private Weather weather;
	}
}
