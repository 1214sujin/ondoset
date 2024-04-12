package com.ondoset.dto.Member;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JwtDTO {

	private String accessToken;
	private String refreshToken;
}
