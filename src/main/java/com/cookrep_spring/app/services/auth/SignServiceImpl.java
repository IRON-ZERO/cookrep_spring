package com.cookrep_spring.app.services.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cookrep_spring.app.dto.auth.request.CreateUserDto;
import com.cookrep_spring.app.dto.auth.response.AuthResponseDto;
import com.cookrep_spring.app.dto.auth.response.ResponseEnum;
import com.cookrep_spring.app.models.user.User;
import com.cookrep_spring.app.repositories.user.UserRepository;
import com.cookrep_spring.app.security.JwtTokenProvider;
import com.cookrep_spring.app.utils.Util;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignServiceImpl implements SignService {
	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public AuthResponseDto loginByNickname(String nickname, String password) {
		User user = userRepo.findByNickname(nickname);
		if (user == null) {
			return authResult(ResponseEnum.FAIL, "등록되지 않은 유저 입니다.");
		}
		boolean matches = passwordEncoder.matches(password, user.getPassword());
		if (!matches) {
			return authResult(ResponseEnum.FAIL, "비밀번호가 일치하지 않습니다.");
		}
		String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getNickname());
		String refreshToken = jwtTokenProvider.createRefresthToken(user.getNickname());
		user.setRefreshToken(refreshToken);
		userRepo.save(user);
		return authResult(ResponseEnum.SUCCESS, "로그인에 성공하였습니다.", accessToken, refreshToken);
	}

	@Override
	public AuthResponseDto loginByEmail(String email, String password) {
		User user = userRepo.findByEmail(email);
		if (user == null) {
			return authResult(ResponseEnum.FAIL, "등록되지 않은 유저 입니다.");
		}
		System.out.println("user :: " + user.getEmail());
		boolean matches = passwordEncoder.matches(password, user.getPassword());
		if (!matches) {
			return authResult(ResponseEnum.FAIL, "비밀번호가 일치하지 않습니다.");
		}
		String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getNickname());
		String refreshToken = jwtTokenProvider.createRefresthToken(user.getNickname());
		user.setRefreshToken(refreshToken);
		userRepo.save(user);
		return authResult(ResponseEnum.SUCCESS, "로그인에 성공하였습니다.", accessToken, refreshToken);
	}

	@Override
	public AuthResponseDto joinUser(CreateUserDto createUserDto) {
		String nickname = createUserDto.getNickname();
		String email = createUserDto.getEmail();
		if (!StringUtils.hasText(email) && !StringUtils.hasText(nickname)) {
			return authResult(ResponseEnum.FAIL, "이메일과 닉네임은 필수입니다.");
		}
		// TODO Auto-generated method stub
		User existUserByEmail = userRepo.findByEmail(email);
		User existUserByName = userRepo.findByNickname(nickname);
		Boolean emailValidate = existUserByEmail != null;
		Boolean usernameValidate = existUserByName != null;

		if (emailValidate) {
			return authResult(ResponseEnum.FAIL, "존재하는 이메일입니다.");
		}

		if (usernameValidate) {
			return authResult(ResponseEnum.FAIL, "존재하는 닉네임입니다.");
		}

		User user = User.builder()
			.userId(Util.UUIDGenerator())
			.email(email)
			.password(passwordEncoder.encode(createUserDto.getPassword()))
			.nickname(nickname)
			.firstName(createUserDto.getFirstName())
			.lastName(createUserDto.getLastName())
			.country(createUserDto.getCountry())
			.city(createUserDto.getCity())
			.build();

		userRepo.save(user);

		return authResult(ResponseEnum.SUCCESS, "회원가입되었습니다.");
	}

	private AuthResponseDto authResult(ResponseEnum statusCode, String msg) {
		return AuthResponseDto.builder().statusCode(statusCode).msg(msg).build();
	}

	private AuthResponseDto authResult(ResponseEnum statusCode, String msg, String access, String refresh) {
		return AuthResponseDto.builder().statusCode(statusCode).msg(msg).access(access).refresh(refresh).build();
	}
}
