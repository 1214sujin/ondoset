package com.ondoset.dto.coordi;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PutRootDTO {

	@Size(min=1)
	private List<Long> clothesList;
}
