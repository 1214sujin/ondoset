package com.ondoset.dto.clothes;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchNameDTO {

	private String category;
	@NotBlank
	private String clothesName;
}
