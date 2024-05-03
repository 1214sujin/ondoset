package com.ondoset.dto.admin.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReportDTO {

	private Long ootdId;
	private Integer reportedCount;
	private String imageURL;
	private List<String> reason;
}
