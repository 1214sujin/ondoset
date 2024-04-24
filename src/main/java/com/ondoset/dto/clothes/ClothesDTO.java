package com.ondoset.dto.clothes;

import com.ondoset.domain.Enum.Category;
import com.ondoset.domain.Enum.Thickness;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClothesDTO {

	private Long clothesId;
	private String name;
	private String imageURL;
	private Category category;
	private String tag;
	private Thickness thickness;
}
