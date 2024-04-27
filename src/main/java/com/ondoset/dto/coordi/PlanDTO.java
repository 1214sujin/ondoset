package com.ondoset.dto.coordi;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PlanDTO {

	@NotNull
	private Long date;
	@Size(min=1)
	private List<Long> clothesList;
}
