package com.ondoset.jwt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ondoset.controller.Advice.ResponseCode;
import com.ondoset.controller.Advice.ResponseMessage;
import com.ondoset.dto.Member.JwtDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStreamReader;

@Log4j2
public class RefreshTokenFilter extends OncePerRequestFilter {

	private final String refreshUrl;
	private final JWTUtil jwtUtil;
	private final Gson gson;

	public RefreshTokenFilter(String refreshUrl, JWTUtil jwtUtil) {
		this.refreshUrl = refreshUrl;
		this.jwtUtil = jwtUtil;
		this.gson = new GsonBuilder().serializeNulls().create();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		String path = request.getRequestURI();

		if (!path.equals(refreshUrl)) {
			filterChain.doFilter(request, response);
			return;
		}

		JwtDTO parameter = gson.fromJson(new InputStreamReader(request.getInputStream()), JwtDTO.class);

		String accessToken = parameter.getAccessToken();
		String refreshToken = parameter.getRefreshToken();

		// Access 토큰 검증 (expired인 경우를 제외)
		try {
			jwtUtil.validateBodyJwt(accessToken);
		} catch (TokenException e) {
			if (e.getTokenErrorCode() != ResponseCode.AUTH4000) {
				e.sendResponseError(response);
				return;
			}
		}

		// refresh 토큰 검증
		try {
			jwtUtil.validateBodyJwt(refreshToken);
		} catch (TokenException e) {
			log.error("Refresh Token 검증 오류: ", e);
			e.sendResponseError(response);
			return;
		}

		String name = jwtUtil.getName(refreshToken);

		String newAccessToken = jwtUtil.createJwt(name, 1L);
		String newRefreshToken = null;

		// refresh token의 만료일이 3일 미만으로 남은 경우 재발급
		if (jwtUtil.getGapTime(refreshToken) < 1000 * 60 * 60 * 24 * 3) {	// 1000 * 60 * 10
			newRefreshToken = jwtUtil.createJwt(name, 30L);
		}

		// 응답 메시지 생성
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");

		JwtDTO res = new JwtDTO();
		res.setAccessToken(newAccessToken);
		res.setRefreshToken(newRefreshToken);

		ResponseMessage<JwtDTO> message = new ResponseMessage<>(ResponseCode.COM2000, res);
		String result = gson.toJson(message);
		response.getWriter().write(result);
	}
}
