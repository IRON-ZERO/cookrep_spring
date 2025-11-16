package com.cookrep_spring.app.services.auth;

import java.util.regex.Pattern;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cookrep_spring.app.models.user.User;
import com.cookrep_spring.app.repositories.user.UserRepository;
import com.cookrep_spring.app.security.CustomUserDetail;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
	private final UserRepository userRepo;
	private final String emailRegex = "[a-zA-Z0-9]([_-]?[a-zA-Z0-9])*@[a-zA-Z0-9]([.]?[a-zA-Z0-9]){2,5}\\.[a-zA-Z]{2,5}";

	@Override
	public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
		boolean matches = Pattern.matches(emailRegex, input);
		User user = matches ? user = userRepo.findByEmail(input) : userRepo.findByNickname(input);
		if (user == null) {
			throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
		}
		return new CustomUserDetail(user);
	}

}
