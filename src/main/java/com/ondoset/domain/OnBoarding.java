package com.ondoset.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OnBoarding extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "on_boarding_id", columnDefinition = "int unsigned")
	private Long id;

	private Integer age;

	private Integer sex;

	private Integer height;

	private Integer weight;

	private Integer activation;

	private Integer exposure;

	@OneToOne(mappedBy = "onBoarding")
	private Member member;
}