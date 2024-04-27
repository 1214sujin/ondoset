package com.ondoset.controller.advice;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseMessage<T> {

	private String code;
	private String message;
	private T result;

	public ResponseMessage(ResponseCode status, T data) {
		this.code = status.getCode();
		this.message = status.getMessage();
		this.result = data;
	}

	public ResponseMessage(ResponseCode status, String message, T data) {
		this.code = status.getCode();
		this.message = message;
		this.result = data;
	}
}
