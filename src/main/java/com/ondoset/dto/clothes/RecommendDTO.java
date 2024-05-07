package com.ondoset.dto.clothes;

import com.ondoset.domain.Enum.Category;
import com.ondoset.domain.Enum.Thickness;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendDTO {

	private Category category;
	private String tag;
	private Long tagId;
	private Thickness thickness;
	private String fullTag;
}
