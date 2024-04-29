package com.ondoset.jwt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.dto.member.LoginDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

@Log4j2
public class LoginFilter extends AbstractAuthenticationProcessingFilter {

	private final AuthenticationManager authenticationManager;
	private final JWTUtil jwtUtil;
	private final Gson gson;

	public LoginFilter(String loginUrl, AuthenticationManager authenticationManager, JWTUtil jwtUtil) {

		super(loginUrl);
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.gson = new GsonBuilder().serializeNulls().create();
	}

	// 사용자 검증
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {

		if (request.getMethod().equalsIgnoreCase("GET")) {
			return null;
		}

		Map<String, String> parameter = gson.fromJson(new InputStreamReader(request.getInputStream()), Map.class);

		String name = parameter.get("username");
		String password = parameter.get("password");

		log.info("username = {}", name);

		if (name.equals("") || password.equals("")) {
			new TokenException(ResponseCode.COM4000).sendResponseError(response);
			return null;
		}

		//스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(name, password, null);

		//token에 담은 검증을 위한 AuthenticationManager로 전달
		return authenticationManager.authenticate(authToken);
	}

	//로그인 성공 시 JWT 발급
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException{

		CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

		String name = customUserDetails.getUsername();
		Boolean isFirst = customUserDetails.getIsFirst();
		Long memberId = customUserDetails.getMemberId();

		String accessToken = jwtUtil.createJwt(name, memberId, 1L);
		String refreshToken = jwtUtil.createJwt(name, memberId, 30L);

		// 응답 메시지 생성
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");

		LoginDTO res = new LoginDTO();
		res.setIsFirst(isFirst);
		res.setMemberId(memberId);
		res.setAccessToken(accessToken);
		res.setRefreshToken(refreshToken);

		ResponseMessage<LoginDTO> message = new ResponseMessage<>(ResponseCode.COM2000, res);
		String result = gson.toJson(message);
		response.getWriter().write(result);
	}

	//로그인 실패 시
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException{

		response.setStatus(401);

		// 응답 메시지 생성
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");

		ResponseMessage<String> message = new ResponseMessage<>(ResponseCode.AUTH4010, "");
		String result = gson.toJson(message);
		response.getWriter().write(result);
	}
}
