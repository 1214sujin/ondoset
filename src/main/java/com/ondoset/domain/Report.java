package com.ondoset.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Setter
@DynamicInsert
public class Report extends BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_id", columnDefinition = "int unsigned")
	private Long id;

	// 게시물 아이디
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ootd_id")
	private OOTD ootd;

	// 신고자
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reporter")
	private Member reporter;

	// 처리 여부
	private Boolean isProcessed;
}
