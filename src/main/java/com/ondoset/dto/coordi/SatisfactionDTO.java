package com.ondoset.dto.coordi;

import com.ondoset.common.Enum;
import com.ondoset.domain.Enum.Satisfaction;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SatisfactionDTO {

	@Enum(enumClass = Satisfaction.class)
	private String satisfaction;
}
