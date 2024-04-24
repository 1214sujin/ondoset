package com.ondoset.dto.admin.blacklist;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetRootDTO {

	private Long memberId;
	private String nickname;
	private Integer banPeriod;
}
