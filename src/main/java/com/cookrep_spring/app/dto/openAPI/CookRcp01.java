package com.cookrep_spring.app.dto.openAPI;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CookRcp01 {
	@JsonProperty("total_count")
	private String totalCount;

	@JsonProperty("row")
	private List<OpenAPIDto> row;

	@JsonProperty("RESULT")
	private Result result;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Result {
		@JsonProperty("MSG")
		private String msg;

		@JsonProperty("CODE")
		private String code;
	}
}
