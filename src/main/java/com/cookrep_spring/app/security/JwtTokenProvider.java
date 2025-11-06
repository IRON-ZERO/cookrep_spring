package com.cookrep_spring.app.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.cookrep_spring.app.services.auth.AuthService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
	private final AuthService authService;

	@Value("${spring.jwt.access-secret}")
	private String accessSecretKey = "accessSecretKey";
	@Value("${spring.jwt.refresh-secret}")
	private String refreshSecretKey = "refreshSecretKey";
	private final long accessTokenValidMilli = 1000l * 60 * 60 * 5;
	private final long refreshTokenValidMilli = 1000l * 60 * 60 * 24 * 7;

	public String createAccessToken(String userId, String loginId) {
		return createToken(userId, loginId, accessTokenValidMilli, accessSecretKey);
	}

	public String createRefresthToken(String loginId) {
		return createToken(null, loginId, refreshTokenValidMilli, refreshSecretKey);
	}

	public Authentication getAuthentication(String token) {
		UserDetails userDetail = authService.loadUserByUsername(this.getUsername(token));
		return new UsernamePasswordAuthenticationToken(userDetail, "");
	}

	public String getUsername(String token) {
		SecretKey key = Keys.hmacShaKeyFor(accessSecretKey.getBytes(StandardCharsets.UTF_8));
		String loginId = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("loginId",
			String.class);
		return loginId;
	}

	public String resolveAccessToken(HttpServletRequest request) {
		return resovleTokenByCookie(request, "access_token");
	}

	public String resolveRefreshToken(HttpServletRequest request) {
		return resovleTokenByCookie(request, "refresh_token");
	}

	public boolean validateToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(accessSecretKey.getBytes(StandardCharsets.UTF_8));
			Claims payload = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
			return !payload.getExpiration().before(new Date());
		} catch (Exception e) {
			return false;
		}
	}

	private String resovleTokenByCookie(HttpServletRequest request, String cookieType) {
		if (request.getCookies() == null) {
			return null;
		}
		for (Cookie c : request.getCookies()) {
			if (c.equals(cookieType)) {
				return c.getValue();
			}
		}
		return null;
	}

	private String createToken(String userId, String loginId, long validTime, String secretKey) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + validTime);
		SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
		String token = Jwts.builder().claim("userId", userId).claim("loginId", loginId).issuedAt(now).expiration(expiry)
			.signWith(key).compact();
		return token;
	}
}
