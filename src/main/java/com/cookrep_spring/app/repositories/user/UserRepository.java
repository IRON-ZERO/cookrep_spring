package com.cookrep_spring.app.repositories.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cookrep_spring.app.models.user.User;

public interface UserRepository extends JpaRepository<User, String> {

	User findByNickname(String nickname);

	User findByEmail(String email);

	User findByUserId(String userId);
}
