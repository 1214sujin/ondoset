package com.ondoset.dto.kma;

import com.ondoset.dto.clothes.FcstDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DayWDTO {

	private Long min;
	private Long max;
	private List<FcstDTO> fcst;
}
