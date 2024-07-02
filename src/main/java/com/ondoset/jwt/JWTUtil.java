package com.ondoset.jwt;

import com.ondoset.controller.advice.ResponseCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Log4j2
@Component
public class JWTUtil {

	private final Key key;

	public JWTUtil(@Value("${com.ondoset.jwt.secret}") String secret) {

		byte[] byteSecretKey = Decoders.BASE64.decode(secret);
		key = Keys.hmacShaKeyFor(byteSecretKey);
	}

	public String getName(String token) {

		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("username", String.class);
	}

	public Long getMemberId(String token) {

		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("memberId", Long.class);
	}

	public String createJwt(String name, Long memberId, Long days) {

		return Jwts.builder()
				.claim("username", name)
				.claim("memberId", memberId)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + days*24*60*60*1000L))	// *60*1000L
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public String validateHeaderJwt(String authorization) throws TokenException {

		// Authorization 헤더 유효성 검증
		if (authorization == null || !authorization.startsWith("Bearer ")) {

			throw new TokenException(ResponseCode.COM4010, "인증 정보가 비어 있습니다.");
		}

		String token = authorization.split(" ")[1];

		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return token;
		}
		catch (ExpiredJwtException e) {

			throw new TokenException(ResponseCode.AUTH4000);
		}
		catch (MalformedJwtException | SignatureException e) {

			throw new TokenException(ResponseCode.AUTH4001);
		}
	}

	public void validateBodyJwt(String token) throws TokenException {

		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
		}
		catch (ExpiredJwtException e) {

			throw new TokenException(ResponseCode.AUTH4000);
		}
		catch (MalformedJwtException | SignatureException e) {

			throw new TokenException(ResponseCode.AUTH4001);
		}
	}
}
