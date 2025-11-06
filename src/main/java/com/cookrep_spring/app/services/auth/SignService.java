package com.cookrep_spring.app.services.auth;

import com.cookrep_spring.app.dto.auth.request.CreateUserDto;
import com.cookrep_spring.app.dto.auth.response.AuthResponseDto;

public interface SignService {

	AuthResponseDto loginByNickname(String nickname, String password);

	AuthResponseDto loginByEmail(String email, String password);

	AuthResponseDto joinUser(CreateUserDto createUserDto);

}
