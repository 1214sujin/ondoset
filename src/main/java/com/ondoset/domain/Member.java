package com.ondoset.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@DynamicInsert
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id", columnDefinition = "int unsigned")
	private Long id;

	// 아이디
	private String name;

	// 비밀번호
	private String password;

	// 닉네임
	private String nickname;

	// ImageURL
	@Column(name = "image_url")
	private String profileImage;

	// 온보딩 여부 (null이면 하지 않음)
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "on_boarding_id")
	private OnBoarding onBoarding;

	// 정지 기간
	private LocalDate banPeriod;

	@OneToMany(mappedBy = "follower")
	private List<Following> followings = new ArrayList<>();
}
