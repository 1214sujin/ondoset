package com.ondoset.dto.member;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginDTO {

	private Boolean isFirst;
	private Long memberId;
	private String accessToken;
	private String refreshToken;
}
