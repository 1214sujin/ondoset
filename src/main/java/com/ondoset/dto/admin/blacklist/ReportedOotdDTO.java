package com.ondoset.dto.admin.blacklist;

import com.ondoset.domain.Enum.Weather;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ReportedOotdDTO {

	private Long ootdId;
	private Long date;
	private Weather weather;
	private Integer lowestTemp;
	private Integer highestTemp;
	private String imageURL;
	private List<String> wearing;
	private String reason;
}
