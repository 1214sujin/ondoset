package com.ondoset.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Wearing extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "wearing_id", columnDefinition = "int unsigned")
	private Long id;

	// 게시물 아이디
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ootd_id")
	private OOTD ootd;

	// 옷 이름
	private String name;

	public void setOOTD(OOTD ootd) {
//        if (this.ootd != null) {
//            this.ootd.getWearings().remove(this);
//        }
		this.ootd = ootd;
		ootd.getWearings().add(this);
	}
}
