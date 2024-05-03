package com.ondoset.dto.admin.tag;

import com.ondoset.common.Enum;
import com.ondoset.domain.Enum.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class TagDTO {

	@Getter
	@Setter
	public static class req {

		@Enum(enumClass = Category.class)
		private String category;
		@NotBlank
		private String tag;
	}

	@Getter
	@Setter
	public static class res {

		private Long tagId;
	}
}
