package com.ondoset.dto.clothes;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SearchNameDTO {

	@Getter
	@Setter
	public static class req {

		private String category;
		@NotBlank
		private String clothesName;
	}

	@Getter
	@Setter
	public static class res {

		private List<ClothesDTO> clothesList;
	}
}
