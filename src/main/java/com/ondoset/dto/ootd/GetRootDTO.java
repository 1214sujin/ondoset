package com.ondoset.dto.ootd;

import com.ondoset.domain.Enum.Weather;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class GetRootDTO {

	@Getter
	@Setter
	public static class req {

		@NotNull
		private Long ootdId;
	}

	@Getter
	@Setter
	public static class res {

		private ProfileShort profileShort;
		private Weather weather;
		private List<String> wearing;
		private Boolean isLike;
	}
}
