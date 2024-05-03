package com.ondoset.dto.clothes;

import com.ondoset.dto.kma.ForecastDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class HomeDTO {

	@Getter
	@Setter
	public static class req {

		@NotNull
		private Long date;
		@NotNull
		private Double lat;
		@NotNull
		private Double lon;
	}

	@Getter
	@Setter
	public static class res {

		private ForecastDTO forecast;
		private List<PlanDTO> plan;
		private List<RecordDTO> record;
		private List<List<RecommendDTO>> recommend;
		private List<OotdShortDTO> ootd;
	}
}
