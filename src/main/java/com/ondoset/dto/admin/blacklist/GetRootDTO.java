package com.ondoset.dto.admin.blacklist;

import lombok.Getter;

import java.time.Duration;

@Getter
public class GetRootDTO {

	private Long memberId;
	private String nickname;
	private Long banPeriod;

	public GetRootDTO(Long memberId, String nickname, Duration banPeriod) {
		this.memberId = memberId;
		this.nickname = nickname;
		this.banPeriod = banPeriod.getSeconds()/(60*60*24);
	}
}
