package com.cookrep_spring.app.controllers.auth;

import org.springframework.http.HttpStatusCode;
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
		AuthResponseDto nicknameLoginResult = signService.loginByNickname(dto.getUserId(), dto.getPassword());
		ResultResponseDto result = result(nicknameLoginResult.getStatusCode(), nicknameLoginResult.getMsg());
		if (result.getStatusCode().getCode() == ResponseEnum.SUCCESS.getCode()) {
			createCookie(response, nicknameLoginResult.getAccess(), nicknameLoginResult.getRefresh());
			return ResponseEntity.ok(result);
		} else {
			return ResponseEntity.status(HttpStatusCode.valueOf(401)).body(result);
		}

	}

	@PostMapping("/loginByEmail")
	public ResponseEntity<ResultResponseDto> loginByEmail(@RequestBody
	LoginUserDto dto, HttpServletResponse response) {
		AuthResponseDto emailLoginResult = signService.loginByEmail(dto.getUserId(), dto.getPassword());
		ResultResponseDto result = result(emailLoginResult.getStatusCode(), emailLoginResult.getMsg());
		if (emailLoginResult.getStatusCode().getCode() == ResponseEnum.SUCCESS.getCode()) {
			createCookie(response, emailLoginResult.getAccess(), emailLoginResult.getRefresh());
			return ResponseEntity.ok(result);
		} else {
			return ResponseEntity.status(HttpStatusCode.valueOf(401)).body(result);
		}
	}

	@PostMapping("/join")
	public ResponseEntity<ResultResponseDto> join(@RequestBody
	CreateUserDto dto) {
		AuthResponseDto createUser = signService.joinUser(dto);
		if (createUser.getStatusCode().getCode() == ResponseEnum.SUCCESS.getCode()) {
			return ResponseEntity.ok(result(createUser.getStatusCode(), createUser.getMsg()));
		} else {
			return ResponseEntity.status(HttpStatusCode.valueOf(400))
				.body(result(createUser.getStatusCode(), createUser.getMsg()));
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<ResultResponseDto> logout(HttpServletRequest request, HttpServletResponse response) {
		AuthResponseDto logoutUser = signService.logoutUser(request);
		if (logoutUser.getStatusCode().getCode() == ResponseEnum.SUCCESS.getCode()) {
			Cookie access = buildCookie("access_token", null, 0);
			Cookie refresh = buildCookie("refresh_token", null, 0);
			response.addCookie(access);
			response.addCookie(refresh);
			return ResponseEntity.ok(result(logoutUser.getStatusCode(), logoutUser.getMsg()));
		} else {
			return ResponseEntity.status(HttpStatusCode.valueOf(400))
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
		Cookie accessCookie = buildCookie("access_token", access, 60 * 60 * 5);
		Cookie refreshCookie = buildCookie("refresh_token", refresh, 60 * 60 * 24 * 7);
		response.addCookie(accessCookie);
		response.addCookie(refreshCookie);
	}

	private Cookie buildCookie(String cookieName, String value, int maxAge) {
		Cookie cookie = new Cookie(cookieName, value);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);
		return cookie;
	}

	private ResultResponseDto result(ResponseEnum responseEnum, String msg) {
		return ResultResponseDto.builder().statusCode(responseEnum).msg(msg).build();
	}
}
