package com.ondoset.dto.admin.blacklist;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class PutRootDTO {

	@Getter
	@Setter
	public static class req {

		@NotNull
		private Integer banPeriod;
	}

	@Getter
	@AllArgsConstructor
	public static class res {

		private Long memberId;
	}
}
