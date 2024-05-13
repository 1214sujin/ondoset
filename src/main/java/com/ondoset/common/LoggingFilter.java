package com.ondoset.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

@Log4j2
@Component
@Order(-10000)
public class LoggingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws IOException {

		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
		ThreadContext.push(String.format("\"%s %s\"", requestWrapper.getMethod(), requestWrapper.getRequestURI()));

		//filter단에서 생긴 오류를 catch
		try {

			filterChain.doFilter(requestWrapper, response);
		} catch (Exception e) {

			log.error(e);

			response.setStatus(500);
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");

			ResponseMessage<String> message = new ResponseMessage<>(ResponseCode.COM5000, "");
			String result = new GsonBuilder().serializeNulls().create().toJson(message);
			response.getWriter().write(result);
		}

		// 입력값 로깅
		StringBuilder requestLog = new StringBuilder();
		ObjectMapper objectMapper = new ObjectMapper();

		requestLog.append("\n\nrequest = ").append(request.getMethod()).append(" ").append(requestWrapper.getRequestURI()).append("\n");
		if (!requestWrapper.getParameterMap().isEmpty()) {
			requestWrapper.getParameterMap().forEach((key, value) -> requestLog.append("\n").append(key).append(" = ").append(Arrays.asList(value).get(0)));
		}
		if (requestWrapper.getContentAsByteArray().length != 0) {
			JsonNode jsonNode = objectMapper.readTree(requestWrapper.getContentAsByteArray());
			for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
				String key = it.next();
				requestLog.append("\n").append(key).append(" = ").append(jsonNode.get(key));
			}
		}
		log.info("{}", requestLog.append("\n").toString());
		ThreadContext.pop();	// 위에서 push한 NDC를 제거
		ThreadContext.clearAll();
	}
}
