package com.ondoset.dto.clothes;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class AllDTO {

	@Getter
	@Setter
	public static class req {

		private Long lastPage;
	}

	@Getter
	@Setter
	public static class res {

		private Long lastPage;
		private List<ClothesDTO> clothesList;
	}
}
