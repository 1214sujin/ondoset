package com.ondoset.dto.Member;

import lombok.Getter;
import lombok.Setter;

public class UsableIdDTO {

	@Getter
	@Setter
	public static class req {

		private String memberId;
	}

	@Getter
	@Setter
	public static class res {

		private Boolean usable;
		private String msg;
	}
}
