package com.ondoset.dto.admin.monitor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ActiveUserDTO {

	private Long period;
	private Long count;
}
