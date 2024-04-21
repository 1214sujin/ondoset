package com.ondoset.dto.kma;

import com.ondoset.domain.Enum.Weather;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NowWDTO {

	private Double now;
	private Double diff;
	private Double feel;
	private Weather weather;
	private int clock;
}
