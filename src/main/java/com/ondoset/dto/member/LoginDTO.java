package com.ondoset.dto.member;

import lombok.Getter;
import lombok.Setter;

public class LoginDTO {

	@Setter
	@Getter
	public static class req {

		private String memberId;
		private String password;
	}

	@Setter
	@Getter
	public static class res {

		private Boolean isFirst;
		private String accessToken;
		private String refreshToken;
	}
}
