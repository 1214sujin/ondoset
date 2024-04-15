package com.ondoset.dto.member;

import lombok.Getter;
import lombok.Setter;

public class UsableNicknameDTO {

	@Getter
	@Setter
	public static class req {

		private String nickname;
	}

	@Getter
	@Setter
	public static class res {

		private Boolean usable;
		private String msg;
	}
}
