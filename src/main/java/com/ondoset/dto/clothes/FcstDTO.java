package com.ondoset.dto.clothes;

import com.ondoset.domain.Enum.Weather;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FcstDTO {

	private Integer time;
	private Long temp;
	private Integer rainP;
	private Weather weather;
}
