package com.ondoset.dto.ootd;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherPreviewDTO {

	@NotNull
	private Double lat;
	@NotNull
	private Double lon;
	@NotNull
	private Long departTime;
	@NotNull
	private Long arrivalTime;
}
