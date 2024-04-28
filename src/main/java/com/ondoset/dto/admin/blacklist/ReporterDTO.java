package com.ondoset.dto.admin.blacklist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReporterDTO {

	private Long memberId;
	private String nickname;
	private Long reportedCount;
}
