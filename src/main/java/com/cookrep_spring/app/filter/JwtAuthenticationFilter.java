package com.cookrep_spring.app.filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cookrep_spring.app.security.JwtTokenProvider;

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

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String accessToken = jwtTokenProvider.resolveAccessToken(request);
		String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
		if (accessToken == null && refreshToken != null && jwtTokenProvider.validateRefreshToken(refreshToken)) {
			String newAccessToken = jwtTokenProvider.refreshingToken(refreshToken);
			Cookie accessCookie = new Cookie("access_token", newAccessToken);
			accessCookie.setHttpOnly(true);
			accessCookie.setSecure(false);
			accessCookie.setPath("/");
			accessCookie.setMaxAge(60 * 60 * 5);
			response.addCookie(accessCookie);
			setAuthentication(newAccessToken);
		}
		if (accessToken != null && jwtTokenProvider.validateAccessToken(accessToken)) {
			setAuthentication(accessToken);
		}
		filterChain.doFilter(request, response);
	}

	private void setAuthentication(String token) {
		Authentication authentication = jwtTokenProvider.getAuthentication(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
