package com.ondoset.domain;

import com.ondoset.domain.Enum.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Tag extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tag_id", columnDefinition = "int unsigned")
	private Long id;

	// 태그 이름
	private String name;

	@Enumerated(EnumType.STRING)
	private Category category;
}
