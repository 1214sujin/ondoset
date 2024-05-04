package com.ondoset.dto.coordi;

import com.ondoset.domain.Enum.Satisfaction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SatisfactionPredDTO {

	@Getter
	@Setter
	public static class req {

		@NotNull
		@Size(min=1)
		private List<FullTagDTO> tagComb;
	}

	@Getter
	@Setter
	public static class res {

		private Satisfaction pred;
	}
}
