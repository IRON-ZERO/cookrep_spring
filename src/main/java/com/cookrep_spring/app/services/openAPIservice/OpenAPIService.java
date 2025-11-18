package com.cookrep_spring.app.services.openAPIservice;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cookrep_spring.app.dto.openAPI.CookRcpResponse;
import com.cookrep_spring.app.dto.openAPI.OpenAPIDto;

import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenAPIService {
	private final WebClient webClient;

	@Value("${api.url}")
	private String baseURL;
	@Value("${api.key}")
	private String apiKey;

	public List<OpenAPIDto> getSlideList(String start_index, String end_index) {
		return getOpenAPIRecipeList(start_index, end_index);
	}

	public List<OpenAPIDto> getOpenAPIRecipeDescList(String start_index, String end_index) {
		List<OpenAPIDto> list = getOpenAPIRecipeList(start_index, end_index);
		List<OpenAPIDto> collect = list.stream().sorted(Comparator.comparing(OpenAPIDto::getRcpSeo).reversed())
			.collect(Collectors.toList());
		return collect;
	}

	private List<OpenAPIDto> getOpenAPIRecipeList(String start_index, String end_index) {
		String formattedString = String.format("/COOKRCP01/json/%s/%s", start_index, end_index);
		String API_URL = baseURL + apiKey + formattedString;
		CookRcpResponse response = webClient.get().uri(API_URL).retrieve().bodyToMono(CookRcpResponse.class).block();
		if (response == null ||
			response.getCOOKRCP01() == null ||
			response.getCOOKRCP01().getRow() == null) {
			return Collections.emptyList();
		}
		return response.getCOOKRCP01().getRow();
	}
}
