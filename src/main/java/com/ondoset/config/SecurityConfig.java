package com.ondoset.config;

import com.ondoset.jwt.AccessTokenFilter;
import com.ondoset.jwt.JWTUtil;
import com.ondoset.jwt.LoginFilter;
import com.ondoset.jwt.RefreshTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final AuthenticationConfiguration authenticationConfiguration;
	private final JWTUtil jwtUtil;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

		return configuration.getAuthenticationManager();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {

		return new BCryptPasswordEncoder();
	}

	@Bean
	@Order(0)
	public SecurityFilterChain nonSecureFilterChain(HttpSecurity http) throws Exception {

		// jwt 없이 접근 가능한 url 및 정적 자원
		http
				.securityMatcher("/member/usable-id", "/member/usable-nickname", "/member/register",
						"/admin/**", "/error", "/images/**")
				.authorizeHttpRequests((auth) -> auth
						.anyRequest().permitAll())
				.securityContext(AbstractHttpConfigurer::disable)
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(AbstractHttpConfigurer::disable);

		return http.build();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.authorizeHttpRequests((auth) -> auth
				.anyRequest().authenticated());

		// jwt를 위한 필터 체인 정의 및 에러 처리
		http
				.addFilterAt(new LoginFilter("/member/login", authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new AccessTokenFilter(jwtUtil), LoginFilter.class)
				.addFilterBefore(new RefreshTokenFilter("/member/jwt", jwtUtil), LoginFilter.class);

		// Cross Site Request Forgery / form login / http basic login 방식 / session 사용 비활성화
		http
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.sessionManagement((session) ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}
}
