package com.ondoset.dto.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicknameDTO {

	@NotBlank
	public String nickname;
}
