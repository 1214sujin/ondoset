package com.ondoset.dto.ootd;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MyProfileDTO {

	private String nickname;
	private Long ootdCount;
	private Long likeCount;
	private Long followingCount;
	private Long lastPage;
	private List<OotdDTO> ootdList;
}
