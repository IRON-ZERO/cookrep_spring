package com.cookrep_spring.app.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cookrep_spring.app.security.JwtTokenProvider;
import com.cookrep_spring.app.services.auth.AuthService;
import com.cookrep_spring.app.utils.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final AuthService authService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();
		return path.startsWith("/api/loginByEmail")
			|| path.startsWith("/api/loginByNickname")
			|| path.equals("/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String accessToken = jwtTokenProvider.resolveAccessToken(request);
		String refreshToken = jwtTokenProvider.resolveRefreshToken(request);

		if (accessToken == null || !jwtTokenProvider.validateAccessToken(accessToken)) {
			if (refreshToken == null) {
				sendUnauthorized(response, "인증정보가 없습니다. 다시 로그인해주세요.");
				return;
			}
			if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
				sendUnauthorized(response, "토큰이 만료되었습니다. 다시 로그인해주세요.");
				return;
			}
			String userId = jwtTokenProvider.getUserIdByRefresh(refreshToken);
			String refreshTokenId = jwtTokenProvider.getRefreshTokenId(refreshToken);
			String userRefreshId = authService.getRefreshId(userId);
			if (!refreshTokenId.equals(userRefreshId)) {
				sendUnauthorized(response, "인증되지 않는 토큰입니다. 다시 로그인해주세요.");
				this.clearRefreshToken(userId, response);
				return;
			}
			String newAccessToken = jwtTokenProvider.refreshingAccessToken(refreshToken);
			Cookie accessCookie = Util.buildCookie(Util.ACCESS_TOKEN, newAccessToken, 60 * 60 * 5);
			response.addCookie(accessCookie);
			setAuthentication(newAccessToken);
		} else {
			if (jwtTokenProvider.validateAccessToken(accessToken)) {
				setAuthentication(accessToken);
			}

		}
		filterChain.doFilter(request, response);
	}

	private void setAuthentication(String token) {
		Authentication authentication = jwtTokenProvider.getAuthentication(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void clearRefreshToken(String userId, HttpServletResponse resposne) {
		authService.clearRefreshToken(userId);
		Cookie access = Util.buildCookie(Util.ACCESS_TOKEN, null, 0);
		Cookie refresh = Util.buildCookie(Util.REFRESH_TOKEN, null, 0);
		resposne.addCookie(access);
		resposne.addCookie(refresh);
	}

	private void sendUnauthorized(HttpServletResponse response, String msg) throws JsonProcessingException, IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");
		Map<String, String> errorMap = new HashMap<>();
		errorMap.put("message", msg);
		response.getWriter().write(objectMapper.writeValueAsString(errorMap));
	}
}
