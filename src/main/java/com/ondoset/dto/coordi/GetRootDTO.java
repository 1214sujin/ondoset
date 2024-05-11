package com.ondoset.dto.coordi;

import com.ondoset.domain.Enum.Satisfaction;
import com.ondoset.domain.Enum.Weather;
import com.ondoset.dto.clothes.ClothesDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class GetRootDTO {

	@Getter
	@Setter
	public static class req {

		@NotNull
		private Integer year;
		@NotNull
		private Integer month;
		private Integer day;
	}

	@Getter
	@Setter
	public static class res {
		
		private Long coordiId;
		private Integer year;
		private Integer month;
		private Integer day;
		private Satisfaction satisfaction;
		private String region;
		private Long departTime;
		private Long arrivalTime;
		private Weather weather;
		private Integer lowestTemp;
		private Integer highestTemp;
		private String imageURL;
		private List<ClothesDTO> clothesList;
	}
}
