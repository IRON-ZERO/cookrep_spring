package com.cookrep_spring.app.controllers.auth;

import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cookrep_spring.app.dto.auth.request.CreateUserDto;
import com.cookrep_spring.app.dto.auth.request.LoginUserDto;
import com.cookrep_spring.app.dto.auth.response.AuthResponseDto;
import com.cookrep_spring.app.dto.auth.response.ResponseEnum;
import com.cookrep_spring.app.dto.auth.response.ResultResponseDto;
import com.cookrep_spring.app.dto.auth.response.UserInfoDto;
import com.cookrep_spring.app.security.CustomUserDetail;
import com.cookrep_spring.app.services.auth.SignServiceImpl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
	private final SignServiceImpl signService;

	@PostMapping("/loginByNickname")
	public ResponseEntity<ResultResponseDto> loginByNickName(@RequestBody
	LoginUserDto dto, HttpServletResponse response) {
		AuthResponseDto result = signService.loginByNickname(dto.getUserId(), dto.getPassword());
		createCookie(response, result.getAccess(), result.getRefresh());
		AuthResponseDto authResponseDto = AuthResponseDto.builder().statusCode(result.getStatusCode()).msg(result.getMsg())
			.data(result.getData())
			.build();
		if (authResponseDto.getStatusCode().getCode() == ResponseEnum.SUCCESS.getCode()) {
			return ResponseEntity.ok(authResponseDto);
		} else {
			return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body(authResponseDto);
		}

	}

	@PostMapping("/loginByEmail")
	public ResponseEntity<AuthResponseDto> loginByEmail(@RequestBody
	LoginUserDto dto, HttpServletResponse response) {
		AuthResponseDto result = signService.loginByEmail(dto.getUserId(), dto.getPassword());
		createCookie(response, result.getAccess(), result.getRefresh());
		AuthResponseDto authResponseDto = AuthResponseDto.builder().statusCode(result.getStatusCode()).msg(result.getMsg())
			.data(result.getData())
			.build();
		if (authResponseDto.getStatusCode().getCode() == ResponseEnum.SUCCESS.getCode()) {
			return ResponseEntity.ok(authResponseDto);
		} else {
			return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body(authResponseDto);
		}
	}

	@PostMapping("/join")
	public ResponseEntity<AuthResponseDto> join(@RequestBody
	CreateUserDto dto) {
		AuthResponseDto createUser = signService.joinUser(dto);
		if (createUser.getStatusCode().getCode() == ResponseEnum.SUCCESS.getCode()) {
			return ResponseEntity.ok(createUser);
		} else {
			return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(createUser);
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<ResultResponseDto> logout(HttpServletRequest request, HttpServletResponse response) {
		AuthResponseDto logoutUser = signService.logoutUser(request);
		System.out.println(logoutUser.getStatusCode());
		if (logoutUser.getStatusCode().getCode() == ResponseEnum.SUCCESS.getCode()) {
			deleteCookie(response, "access_token");
			deleteCookie(response, "refresh_token");
			return ResponseEntity.ok(result(logoutUser.getStatusCode(), logoutUser.getMsg()));
		} else {
			return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST)
				.body(result(logoutUser.getStatusCode(), logoutUser.getMsg()));
		}
	}

	@GetMapping("/check")
	public ResponseEntity<UserInfoDto> validateUser(@AuthenticationPrincipal
	CustomUserDetail user, HttpServletRequest request) {
		if (user == null) {
			return ResponseEntity.ok(null);
		}
		return ResponseEntity.ok(UserInfoDto.builder().userId(user.getUserId()).userNickname(user.getUsername())
			.userEmail(user.getUserEmail()).build());
	}

	private void createCookie(HttpServletResponse response, String access, String refresh) {
		Cookie accessCookie = new Cookie("access_token", access);
		accessCookie.setHttpOnly(true);
		accessCookie.setSecure(false);
		accessCookie.setPath("/");
		accessCookie.setMaxAge(60 * 60 * 5);
		Cookie refreshCookie = new Cookie("refresh_token", refresh);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(false);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(60 * 60 * 24 * 7);
		response.addCookie(accessCookie);
		response.addCookie(refreshCookie);
	}

	private void deleteCookie(HttpServletResponse response, String cookieName) {
		Cookie cookie = new Cookie(cookieName, null);
		cookie.setHttpOnly(true);
		cookie.setSecure(false);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	private ResultResponseDto result(ResponseEnum responseEnum, String msg) {
		return ResultResponseDto.builder().statusCode(responseEnum).msg(msg).build();
	}
}
