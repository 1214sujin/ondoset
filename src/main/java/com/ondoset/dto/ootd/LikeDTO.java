package com.ondoset.dto.ootd;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeDTO {

	@NotNull
	private Long ootdId;
}
