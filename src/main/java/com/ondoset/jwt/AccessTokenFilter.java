package com.ondoset.jwt;

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

@Log4j2
@AllArgsConstructor
public class AccessTokenFilter extends OncePerRequestFilter {

	private JWTUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		// 로그인 url 처리
		String path = request.getRequestURI();
		log.info(path);

		if (path.equals("/member/login") || path.equals("/member/jwt")) {
			log.info("path = {}", path);
			filterChain.doFilter(request, response);
			return;
		}

		String authorization = request.getHeader("Authorization");

		String token;

		// 토큰 만료 여부 검증
		try {
			token = jwtUtil.validateHeaderJwt(authorization);
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
