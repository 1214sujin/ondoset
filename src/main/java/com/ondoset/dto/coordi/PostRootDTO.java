package com.ondoset.dto.coordi;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PostRootDTO {

	@NotNull
	private Double lat;
	@NotNull
	private Double lon;
	@NotNull
	private Long departTime;
	@NotNull
	private Long arrivalTime;
	@Size(min=1)
	private List<Long> clothesList;
}
