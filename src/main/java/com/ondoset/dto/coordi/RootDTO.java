package com.ondoset.dto.coordi;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RootDTO {

	@Setter
	@Getter
	public static class req {

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

	@Setter
	@Getter
	public static class res {

		private Long date;
	}
}
