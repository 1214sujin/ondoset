package com.ondoset.dto.coordi;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OutTimeDTO {

	@NotNull
	private Double lat;
	@NotNull
	private Double lon;
	@NotNull
	private Long departTime;
	@NotNull
	private Long arrivalTime;
}
