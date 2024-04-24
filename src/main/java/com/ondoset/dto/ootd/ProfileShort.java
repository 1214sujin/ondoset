package com.ondoset.dto.ootd;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileShort {

	private Long memberId;
	private String nickname;
	private String imageURL;
	private Boolean isFollowing;
	private Long ootdCount;
}
