package com.ondoset.config;

import com.google.gson.GsonBuilder;
import com.ondoset.common.LoggingFilter;
import com.ondoset.controller.advice.ResponseCode;
import com.ondoset.controller.advice.ResponseMessage;
import com.ondoset.jwt.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Log4j2
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final CustomUserDetailsService customUserDetailsService;
	private final AuthenticationEntryPoint AdminAuthenticationEntryPoint;
	private final JWTUtil jwtUtil;
	private final LoggingFilter loggingFilter;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {

		return new BCryptPasswordEncoder();
	}

	@Bean
	@Order(0)
	public SecurityFilterChain nonSecureFilterChain(HttpSecurity http) throws Exception {

		http	// 관리 URL 설정
				.securityMatcher("/member/usable-id", "/member/usable-nickname", "/member/register",
						"/error", "/images/**")
				.authorizeHttpRequests((auth) -> auth
						.anyRequest().permitAll())
				.securityContext(AbstractHttpConfigurer::disable)
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(AbstractHttpConfigurer::disable);

		return http.build();
	}

	@Bean
	@Order(1)
	SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {

		AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder
				.userDetailsService(new InMemoryUserDetailsManager(User.withUsername("admin")
						.password(passwordEncoder().encode("admin1234"))
						.build()));

		AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

		http	// 기타 설정
				.authenticationManager(authenticationManager)
				.csrf(AbstractHttpConfigurer::disable);

		http	// 관리 URL 설정
				.securityMatcher("/admin/**")
				.authorizeHttpRequests((auth) -> auth
						.anyRequest().authenticated())
				.httpBasic(Customizer.withDefaults());

		http	// 로그인 설정
				.formLogin((auth) -> auth
						.loginProcessingUrl("/admin/auth/login")
						// 로그인 성공 시 핸들러
						.successHandler((httpServletRequest, response, authentication) -> {
							response.setContentType("application/json");
							response.setCharacterEncoding("utf-8");

							ResponseMessage<String> message = new ResponseMessage<>(ResponseCode.COM2000, "로그인 성공");
							String result = new GsonBuilder().serializeNulls().create().toJson(message);
							response.getWriter().write(result);
						})
						// 로그인 실패 시 핸들러
						.failureHandler((httpServletRequest, response, authentication) -> {
							log.warn(ResponseCode.AUTH4010.getMessage());
							response.setStatus(401);
							response.setContentType("application/json");
							response.setCharacterEncoding("utf-8");

							ResponseMessage<String> message = new ResponseMessage<>(ResponseCode.AUTH4010, "");
							String result = new GsonBuilder().serializeNulls().create().toJson(message);
							response.getWriter().write(result);
						})
						.permitAll());

		http	// 로그아웃 설정
				.logout((auth) -> auth
						.logoutUrl("/admin/auth/logout")
						.logoutSuccessHandler((httpServletRequest, response, authentication) -> {
							response.setContentType("application/json");
							response.setCharacterEncoding("utf-8");

							ResponseMessage<String> message = new ResponseMessage<>(ResponseCode.COM2000, "로그아웃 성공");
							String result = new GsonBuilder().serializeNulls().create().toJson(message);
							response.getWriter().write(result);
						}));

		http	// 미인증 사용자 접속 시
				.exceptionHandling(auth ->
						auth.authenticationEntryPoint(AdminAuthenticationEntryPoint));

		return http.build();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder
				.userDetailsService(customUserDetailsService)
				.passwordEncoder(passwordEncoder());

		AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

		http
				.authenticationManager(authenticationManager)
				// Cross Site Request Forgery / form login / http basic login 방식 / session 사용 비활성화
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.sessionManagement((session) ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http	// 관리 URL 설정 (위에서 설정한 것 외 전체)
				.authorizeHttpRequests((auth) -> auth
						.anyRequest().authenticated());

		// jwt를 위한 필터 체인 정의 및 에러 처리
		http
				.addFilterBefore(new LoginFilter("/member/login", authenticationManager, jwtUtil), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new AccessTokenFilter(jwtUtil), LoginFilter.class)
				.addFilterBefore(new RefreshTokenFilter("/member/jwt", jwtUtil), LoginFilter.class);

		return http.build();
	}
}
