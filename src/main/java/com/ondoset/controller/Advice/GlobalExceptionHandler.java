package com.ondoset.controller.Advice;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler
	public ResponseEntity<ResponseMessage<String>> HandelCustomException(CustomException e) {

		log.error(e.getMessage());
		ResponseCode responseCode = e.getResponseCode();
		return handleExceptionInternal(responseCode);
	}

	@ExceptionHandler
	public ResponseEntity<ResponseMessage<String>> HandleAccessDeniedException(AccessDeniedException e) {

		log.error(e.getMessage());
		return handleExceptionInternal(ResponseCode.COM4030);
	}

	@ExceptionHandler
	public ResponseEntity<ResponseMessage<String>> HandleDataIntegrityViolationException(DataIntegrityViolationException e) {

		log.error(e.getMessage());
		return handleExceptionInternal(ResponseCode.COM4090);
	}

	private ResponseEntity<ResponseMessage<String>> handleExceptionInternal (ResponseCode responseCode) {
		return ResponseEntity.status(responseCode.getStatus()).body(new ResponseMessage<>(responseCode, ""));
	}

	@Override
	public ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request){
		return ResponseEntity.status(500).body(new ResponseMessage<>(ResponseCode.COM5000, ""));
	}
}
