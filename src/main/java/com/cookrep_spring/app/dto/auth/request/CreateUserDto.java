package com.cookrep_spring.app.dto.auth.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {
	private String email;
	private String password;
	private String nickname;
	private String firstName;
	private String lastName;
	private String country;
	private String city;
}
