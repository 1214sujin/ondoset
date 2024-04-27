package com.ondoset.config;

import com.google.gson.GsonBuilder;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AdminAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

		response.setStatus(401);
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");

		ResponseMessage<String> message = new ResponseMessage<>(ResponseCode.COM4010, "");
		String result = new GsonBuilder().serializeNulls().create().toJson(message);
		response.getWriter().write(result);
	}
}
