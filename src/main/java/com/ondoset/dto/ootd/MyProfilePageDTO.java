package com.ondoset.dto.ootd;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MyProfilePageDTO {

	private Long lastPage;
	private List<OotdDTO> ootdList;
}
