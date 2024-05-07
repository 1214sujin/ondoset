package com.ondoset.dto.coordi;

import com.ondoset.common.Enum;
import com.ondoset.domain.Enum.Thickness;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FullTagDTO {

	@NotNull
	private Long tagId;
	@Enum(enumClass = Thickness.class)
	private String thickness;
}
