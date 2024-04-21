package com.ondoset.dto.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter	// NoArgsConstructor가 필요한데, 자동으로 만들어줌
@Getter
public class RegisterDTO {

	@NotBlank
	private String memberId;	// name
	@NotBlank
	private String password;
	@NotBlank
	private String nickname;
}
