package com.ondoset.dto.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class UsableIdDTO {

	@Getter
	@Setter
	public static class req {

		@NotBlank
		private String memberId;
	}

	@Getter
	@Setter
	public static class res {

		private Boolean usable;
		private String msg;
	}
}
