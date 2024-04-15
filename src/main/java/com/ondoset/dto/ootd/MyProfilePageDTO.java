package com.ondoset.dto.ootd;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class MyProfilePageDTO {

	@Getter
	@Setter
	public static class req {

		private Long lastPage;
	}

	@Getter
	@Setter
	public static class res {

		private Long lastPage;
		private List<OotdDTO> ootdList;
	}
}
