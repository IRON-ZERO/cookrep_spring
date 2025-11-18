package com.cookrep_spring.app.dto.openAPI;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CookRcpResponse {
	@JsonProperty("COOKRCP01")
	private CookRcp01 COOKRCP01;
}
