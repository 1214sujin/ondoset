package com.ondoset.controller.advice;

import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler
	public ResponseEntity<ResponseMessage<String>> HandelCustomException(CustomException e) {

		ResponseCode responseCode = e.getResponseCode();

		if (e.getMessage().equals("")) {
			return handleExceptionInternal(responseCode);
		}
		else {
			return handleExceptionInternal(responseCode, e.getMessage());
		}
	}

	@ExceptionHandler
	public ResponseEntity<ResponseMessage<String>> HandleAccessDeniedException(AccessDeniedException e) {
		return handleExceptionInternal(ResponseCode.COM4030);
	}

	@ExceptionHandler
	public ResponseEntity<ResponseMessage<String>> HandleDataIntegrityViolationException(DataIntegrityViolationException e) {
		return handleExceptionInternal(ResponseCode.DB5000);
	}

	@ExceptionHandler
	public ResponseEntity<ResponseMessage<String>> HandleNoSuchElementException(NoSuchElementException e) {
		return handleExceptionInternal(ResponseCode.COM4091);
	}

	private ResponseEntity<ResponseMessage<String>> handleExceptionInternal (ResponseCode responseCode) {
		log.error(responseCode.getMessage());
		return ResponseEntity.status(responseCode.getStatus()).body(new ResponseMessage<>(responseCode, ""));
	}

	private ResponseEntity<ResponseMessage<String>> handleExceptionInternal (ResponseCode responseCode, String message) {
		log.error(responseCode.getMessage() + message);
		return ResponseEntity.status(responseCode.getStatus()).body(new ResponseMessage<>(responseCode, responseCode.getMessage() + message, ""));
	}

	@Override
	public ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request){
		log.error(ex.getMessage());

		if (ex.getClass() == MethodArgumentNotValidException.class) {
			return ResponseEntity.badRequest().body(new ResponseMessage<>(ResponseCode.COM4000, ""));
		}
		if (ex.getClass() == HttpRequestMethodNotSupportedException.class) {
			return ResponseEntity.badRequest().body(new ResponseMessage<>(ResponseCode.COM4050, ResponseCode.COM4050.getMessage() + ((HttpRequestMethodNotSupportedException) ex).getMethod() , ""));
		}
		return ResponseEntity.status(500).body(new ResponseMessage<>(ResponseCode.COM5000, ""));
	}
}
