package com.ondoset.dto.admin.blacklist;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ReporterListDTO {

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
		private List<ReportedOotdDTO> ootdList;
	}
}
