package com.ondoset.dto.member;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OnBoardingDTO {

	@Size(min=6, max=6)
	private Integer[] answer;
}
