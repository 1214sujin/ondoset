package com.ondoset.dto.ootd;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OotdDTO {

	private Long ootdId;
	private Long date;
	private Integer lowestTemp;
	private Integer highestTemp;
	private String imageURL;
}
