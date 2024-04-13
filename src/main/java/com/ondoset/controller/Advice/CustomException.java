package com.ondoset.controller.Advice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class CustomException extends RuntimeException {

	private final ResponseCode responseCode;
	private String message = "";
}
