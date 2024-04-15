package com.ondoset.dto.member;

import lombok.Getter;
import lombok.Setter;

@Setter	// NoArgsConstructor가 필요한데, 자동으로 만들어줌
@Getter
public class RegisterDTO {

	private String memberId;	// name
	private String password;
	private String nickname;
}
