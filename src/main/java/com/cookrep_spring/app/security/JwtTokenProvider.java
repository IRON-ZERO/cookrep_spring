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
import com.cookrep_spring.app.utils.Util;

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
	private String accessSecretKey;
	@Value("${spring.jwt.refresh-secret}")
	private String refreshSecretKey;

	private static final long MILLISECONDS_PER_SECOND = 1000l;
	private static final int SECONDS_PER_MIN = 60;
	private static final int MINUTES_PER_HOUR = 60;
	private static final int HOURS_PER_DAY = 24;
	private static final int ACCESS_TOKEN_HOURS = 5;
	private static final int REFRESH_TOKEN_DAYS = 7;

	private final long ACCESS_TOKEN_VALIDATE_MILLISEC = MILLISECONDS_PER_SECOND * SECONDS_PER_MIN * MINUTES_PER_HOUR
		* ACCESS_TOKEN_HOURS;
	private final long REFRESH_TOKEN_VALIDATE_MILLISEC = MILLISECONDS_PER_SECOND * SECONDS_PER_MIN * MINUTES_PER_HOUR
		* HOURS_PER_DAY * REFRESH_TOKEN_DAYS;

	public String createAccessToken(String userId, String loginId) {
		return createToken(userId, loginId, ACCESS_TOKEN_VALIDATE_MILLISEC, accessSecretKey);
	}

	public String createRefreshToken(String userId, String loginId, String refreshId) {
		return createToken(userId, loginId, REFRESH_TOKEN_VALIDATE_MILLISEC, refreshSecretKey, refreshId);
	}

	public Authentication getAuthentication(String token) {
		UserDetails userDetail = authService.loadUserByUsername(this.getUsernameByAccess(token));
		return new UsernamePasswordAuthenticationToken(userDetail, "", userDetail.getAuthorities());
	}

	public boolean validateAccessToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(accessSecretKey.getBytes(StandardCharsets.UTF_8));
			Claims payload = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
			return !payload.getExpiration().before(new Date());
		} catch (Exception e) {
			return false;
		}
	}

	public boolean validateRefreshToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(refreshSecretKey.getBytes(StandardCharsets.UTF_8));
			Claims payload = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
			return !payload.getExpiration().before(new Date());
		} catch (Exception e) {
			return false;
		}
	}

	public String refreshingAccessToken(String refreshToken) {
		String userId = this.getUserIdByRefresh(refreshToken);
		String username = this.getUsernameByRefresh(refreshToken);
		String accessToken = createAccessToken(userId, username);
		return accessToken;
	}

	public String getUsernameByAccess(String token) {
		SecretKey key = Keys.hmacShaKeyFor(accessSecretKey.getBytes(StandardCharsets.UTF_8));
		String loginId = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("loginId",
			String.class);
		return loginId;
	}

	public String getUserIdByAccess(String token) {
		SecretKey key = Keys.hmacShaKeyFor(accessSecretKey.getBytes(StandardCharsets.UTF_8));
		String userId = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("userId",
			String.class);
		return userId;
	}

	public String getUsernameByRefresh(String token) {
		SecretKey key = Keys.hmacShaKeyFor(refreshSecretKey.getBytes(StandardCharsets.UTF_8));
		String loginId = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("loginId",
			String.class);
		return loginId;
	}

	public String getUserIdByRefresh(String token) {
		SecretKey key = Keys.hmacShaKeyFor(refreshSecretKey.getBytes(StandardCharsets.UTF_8));
		String userId = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("userId",
			String.class);
		return userId;
	}

	public String getRefreshTokenId(String token) {
		SecretKey key = Keys.hmacShaKeyFor(refreshSecretKey.getBytes(StandardCharsets.UTF_8));
		String refreshId = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("refreshId",
			String.class);
		return refreshId;
	}

	private String resolveTokenByCookie(HttpServletRequest request, String cookieType) {
		if (request.getCookies() == null) {
			return null;
		}
		for (Cookie c : request.getCookies()) {
			if (c.getName().equals(cookieType)) {
				return c.getValue();
			}
		}
		return null;
	}

	public String resolveAccessToken(HttpServletRequest request) {
		return resolveTokenByCookie(request, Util.ACCESS_TOKEN);
	}

	public String resolveRefreshToken(HttpServletRequest request) {
		return resolveTokenByCookie(request, Util.REFRESH_TOKEN);
	}

	private String createToken(String userId, String loginId, long validTime, String secretKey) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + validTime);
		SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
		String token = Jwts.builder().claim("userId", userId).claim("loginId", loginId).issuedAt(now).expiration(expiry)
			.signWith(key).compact();
		return token;
	}

	private String createToken(String userId, String loginId, long validTime, String secretKey, String refreshId) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + validTime);
		SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
		String token = Jwts.builder().claim("userId", userId).claim("loginId", loginId).claim("refreshId", refreshId)
			.issuedAt(now).expiration(expiry)
			.signWith(key).compact();
		return token;
	}
}
