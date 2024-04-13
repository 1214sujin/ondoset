package com.ondoset.jwt;

import com.ondoset.controller.Advice.ResponseCode;
import com.ondoset.domain.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@Log4j2
@AllArgsConstructor
public class AccessTokenFilter extends OncePerRequestFilter {

	private JWTUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		// jwt가 불필요한  url 처리
		String path = request.getRequestURI();
		ArrayList<String> authPass = new ArrayList<>(Arrays.asList("/member/usable-id", "/member/usable-nickname",
				"/member/register", "/member/login", "/member/jwt", "/error"));

		if (authPass.contains(path) | path.startsWith("/auth/")) {
			log.info("path = {}", path);
			filterChain.doFilter(request, response);
			return;
		}

		String authorization = request.getHeader("Authorization");

		// Authorization 헤더 유효성 검증
		if (authorization == null || !authorization.startsWith("Bearer ")) {

			log.warn("token is null or non-validated");
			TokenException e = new TokenException(ResponseCode.COM4010);
			e.sendResponseError(response);
//			filterChain.doFilter(request, response);

			return;
		}

		String token = authorization.split(" ")[1];

		// 토큰 만료 여부 검증
		try {
			jwtUtil.validateJwt(token);
		}
		catch (TokenException e) {
			log.warn("token expired");
			e.sendResponseError(response);

			return;
		}

		// 토큰에서 username 획득. 리포지토리에 접근하지 않음
		String name = jwtUtil.getName(token);
		log.info(name);

		Member member = new Member();
		member.setName(name);

		CustomUserDetails customUserDetails = new CustomUserDetails(member);

		Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, null);
		// 사용자 세션 생성
		SecurityContextHolder.getContext().setAuthentication(authToken);

		filterChain.doFilter(request, response);
	}
}
