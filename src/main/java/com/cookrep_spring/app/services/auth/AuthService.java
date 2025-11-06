package com.cookrep_spring.app.services.auth;

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

	@Override
	public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
		User user;
		if (input.contains("@")) {
			user = userRepo.findByEmail(input);
		} else {
			user = userRepo.findByNickname(input);
		}
		return new CustomUserDetail(user);
	}

}
