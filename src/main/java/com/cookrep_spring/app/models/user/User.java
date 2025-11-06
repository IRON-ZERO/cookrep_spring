package com.cookrep_spring.app.models.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	private String userId;
	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;
	@Column(nullable = false, unique = true)
	private String nickname;
	private String firstName;
	private String lastName;
	private String country;
	private String city;
	private String refreshToken;
}
