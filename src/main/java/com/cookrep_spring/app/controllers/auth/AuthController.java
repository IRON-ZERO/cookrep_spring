package com.cookrep_spring.app.controllers.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cookrep_spring.app.dto.auth.request.CreateUserDto;
import com.cookrep_spring.app.dto.auth.request.LoginUserDto;
import com.cookrep_spring.app.dto.auth.response.AuthResponseDto;
import com.cookrep_spring.app.services.auth.SignServiceImpl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
	private final SignServiceImpl signService;

	@PostMapping("/loginByNickname")
	public AuthResponseDto loginByNickName(@RequestBody
	LoginUserDto dto, HttpServletResponse response) {
		AuthResponseDto result = signService.loginByNickname(dto.getUserId(), dto.getPassword());
		createCookie(response, result.getAccess(), result.getRefresh());
		AuthResponseDto authResponseDto = AuthResponseDto.builder().statusCode(result.getStatusCode()).msg(result.getMsg())
			.build();
		return authResponseDto;
	}

	@PostMapping("/loginByEmail")
	public AuthResponseDto loginByEmail(@RequestBody
	LoginUserDto dto, HttpServletResponse response) {
		AuthResponseDto result = signService.loginByEmail(dto.getUserId(), dto.getPassword());
		createCookie(response, result.getAccess(), result.getRefresh());
		AuthResponseDto authResponseDto = AuthResponseDto.builder().statusCode(result.getStatusCode()).msg(result.getMsg())
			.build();
		return authResponseDto;
	}

	@PostMapping("/join")
	public AuthResponseDto join(@RequestBody
	CreateUserDto dto) {
		return signService.joinUser(dto);
	}

	private void createCookie(HttpServletResponse response, String access, String refresh) {
		Cookie accessCookie = new Cookie("access_token", access);
		accessCookie.setHttpOnly(true);
		accessCookie.setSecure(true);
		accessCookie.setPath("/");
		accessCookie.setMaxAge(60 * 60 * 5);
		System.out.println("access :: " + accessCookie.getValue());
		Cookie refreshCookie = new Cookie("refresh_token", refresh);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(true);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(60 * 60 * 24 * 7);
		response.addCookie(accessCookie);
		response.addCookie(refreshCookie);
	}
}
