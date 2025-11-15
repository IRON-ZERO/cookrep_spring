package com.cookrep_spring.app.services.auth;

import com.cookrep_spring.app.dto.auth.request.CreateUserDto;
import com.cookrep_spring.app.dto.auth.response.AuthResponseDto;

import jakarta.servlet.http.HttpServletRequest;

public interface SignService {

	AuthResponseDto loginByNickname(String nickname, String password);

	AuthResponseDto loginByEmail(String email, String password);

	AuthResponseDto joinUser(CreateUserDto createUserDto);

	AuthResponseDto logoutUser(HttpServletRequest request);

}
