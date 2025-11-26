package com.cookrep_spring.app.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ResultResponseDto {
	private ResponseEnum statusCode;
	private String msg;
}
