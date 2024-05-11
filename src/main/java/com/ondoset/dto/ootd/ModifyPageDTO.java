package com.ondoset.dto.ootd;

import com.ondoset.domain.Enum.Weather;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ModifyPageDTO {

	private Long ootdId;
	private String region;
	private Long departTime;
	private Long arrivalTime;
	private Weather weather;
	private Integer lowestTemp;
	private Integer highestTemp;
	private String imageURL;
	private List<String> wearingList;
}
