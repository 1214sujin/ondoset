package com.ondoset.dto.coordi;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RootDTO {

	@Setter
	@Getter
	public static class req {

		private Double lat;
		private Double lon;
		private Long departTime;
		private Long arrivalTime;
		private List<Long> clothesList;
	}

	@Setter
	@Getter
	public static class res {

		private Long date;
	}
}
