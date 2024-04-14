package com.ondoset.jwt;

import com.google.gson.Gson;
import com.ondoset.controller.Advice.ResponseCode;
import com.ondoset.controller.Advice.ResponseMessage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@Getter
public class TokenException extends RuntimeException {

	ResponseCode tokenErrorCode;

	public TokenException(ResponseCode error) {
		super(error.getMessage());
		this.tokenErrorCode = error;
	}

	public void sendResponseError(HttpServletResponse response) throws IOException {

		log.error(tokenErrorCode.getMessage());

		response.setStatus(tokenErrorCode.getStatus());

		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");

		ResponseMessage<String> message = new ResponseMessage<>(tokenErrorCode, "");
		String result = new Gson().toJson(message);
		response.getWriter().write(result);
	}
}
