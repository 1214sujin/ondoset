package com.ondoset.dto.admin.monitor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogDTO {

	private String date;
	private String level;
	private String location;
	private String msg;
}
