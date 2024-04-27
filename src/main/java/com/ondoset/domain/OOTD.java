package com.ondoset.domain;

import com.ondoset.domain.Enum.TempRate;
import com.ondoset.domain.Enum.Weather;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@DynamicInsert
public class OOTD extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ootd_id", columnDefinition = "int unsigned")
	private Long id;

	// 작성자
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	// 나간 시간
	@Column(columnDefinition = "bigint")
	private Long departTime;

	// 들어온 시간
	@Column(columnDefinition = "bigint")
	private Long arrivalTime;

	// 날씨
	@Enumerated(EnumType.STRING)
	private Weather weather;

	// 최저 기온
	private Integer lowestTemp;

	// 최고 기온
	private Integer highestTemp;

	// 기온 범위. 미리 계산해놔야 검색이 수월해질 것 같음
	@Enumerated(EnumType.STRING)
	private TempRate tempRate;

	// 게시물 이미지
	@Column(name = "image_url")
	private String imageURL;

	// 신고된 횟수
	private Integer reportedCount;

	// 가리기 여부
	private Boolean isBlinded;

	@OneToMany(mappedBy = "ootd")
	private List<Wearing> wearings = new ArrayList<>();

	@OneToMany(mappedBy = "ootd")
	private List<Like> likes = new ArrayList<>();
}
