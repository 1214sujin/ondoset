package com.ondoset.common;

import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Clob;
import java.util.Date;

@Getter
@Entity
@Table(name="log")
public class LogEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "log_id", columnDefinition = "int unsigned")
	private Long id;

	private Date date;

	@Column(length = 5)
	private String level;

	private String location;

	@Column(columnDefinition = "text")
	private Clob msg;
}
