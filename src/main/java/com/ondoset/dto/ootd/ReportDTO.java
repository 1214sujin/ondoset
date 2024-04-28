package com.ondoset.dto.ootd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDTO {

	@NotNull
	private Long ootdId;
	@NotBlank
	private String reason;
}
