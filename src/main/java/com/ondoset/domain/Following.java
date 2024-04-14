package com.ondoset.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="`following`")
public class Following extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "following_id", columnDefinition = "int unsigned")
	private Long id;

	// 팔로우하는 사람
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "follower")
	private Member follower;

	// 팔로우 받는 사람. 항상 닉네임까지 다 불러올 필요는 없으므로 LAZY (ID부터 확인해야 하는 경우가 존재)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "followed")
	private Member followed;
}
