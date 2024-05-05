package com.ondoset.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

@Log4j2
@Component
public class LoggingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
		ThreadContext.push(String.format("\"%s %s\"", requestWrapper.getMethod(), requestWrapper.getRequestURI()));

		filterChain.doFilter(requestWrapper, response);

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
