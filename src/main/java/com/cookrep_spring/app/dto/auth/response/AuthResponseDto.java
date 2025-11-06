package com.cookrep_spring.app.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponseDto {
	private ResponseEnum statusCode;
	private String msg;
	private String access;
	private String refresh;
}
