package com.cookrep_spring.app.services.openAPIservice;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cookrep_spring.app.dto.openAPI.CookRcpResponse;
import com.cookrep_spring.app.dto.openAPI.OpenAPIDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

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

	public List<OpenAPIDto> getOpenAPIRecipeSortDescList(String start_index, String end_index) {
		List<OpenAPIDto> list = getOpenAPIRecipeList(start_index, end_index);
		List<OpenAPIDto> collect = list.stream().sorted(Comparator.comparing(OpenAPIDto::getRcpSeo).reversed())
			.collect(Collectors.toList());
		return collect;
	}

	//Mono로 리턴
	private List<OpenAPIDto> getOpenAPIRecipeList(String start_index, String end_index) {
		String formattedString = String.format("/COOKRCP01/json/%s/%s", start_index, end_index);
		String API_URL = baseURL + apiKey + formattedString;
		CookRcpResponse response = webClient.get().uri(API_URL).retrieve()
			// 1) HTTP 4xx 에러 처리
			.onStatus(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse.bodyToMono(String.class)
				.flatMap(body -> Mono.error(new RuntimeException("클라이언트 오류: " + body))))

			// 2) HTTP 5xx 에러 처리
			.onStatus(HttpStatusCode::is5xxServerError, clientResponse -> clientResponse.bodyToMono(String.class)
				.flatMap(body -> Mono.error(new RuntimeException("서버 오류: " + body))))

			.bodyToMono(CookRcpResponse.class).block();
		if (response == null ||
			response.getCOOKRCP01() == null ||
			response.getCOOKRCP01().getRow() == null) {
			return Collections.emptyList();
		}
		return response.getCOOKRCP01().getRow();
	}

}
