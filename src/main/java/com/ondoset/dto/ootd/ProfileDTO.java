package com.ondoset.dto.ootd;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ProfileDTO {

	@Getter
	@Setter
	public static class req {

		@NotNull
		private Long memberId;
		@NotNull
		private Long lastPage;
	}

	@Getter
	@Setter
	public static class res {

		private Long lastPage;
		private List<OotdDTO> ootdList;
	}
}
