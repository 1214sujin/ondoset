package com.ondoset.domain.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TempRate {

	T28("28°C~"),
	T23("23~27°C"),
	T20("20~22°C"),
	T17("17~19°C"),
	T12("12~16°C"),
	T9("9~11°C"),
	T5("5~8°C"),
	TELSE("~4°C");

	private final String name;

	public static TempRate valueOfLower(String name) {
		return TempRate.valueOf(name.toUpperCase());
	}
}
