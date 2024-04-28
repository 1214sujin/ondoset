package com.ondoset.dto.ootd;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowDTO {

	@NotNull
	private Long memberId;
}
