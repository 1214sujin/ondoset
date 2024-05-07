package com.ondoset.dto.clothes;

import com.ondoset.common.Enum;
import com.ondoset.domain.Enum.Thickness;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SearchTagDTO {

	@Getter
	@Setter
	public static class req {

		@Enum(enumClass = Thickness.class, nullable = true)
		private String thickness;
		@NotNull
		private Long tagId;
	}

	@Getter
	@Setter
	public static class res {

		private List<ClothesShortDTO> clothesShortList;
		private String coupangURL;
	}
}
