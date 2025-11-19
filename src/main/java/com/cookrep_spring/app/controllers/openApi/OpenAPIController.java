package com.cookrep_spring.app.controllers.openApi;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cookrep_spring.app.dto.openAPI.OpenAPIDto;
import com.cookrep_spring.app.services.openAPIservice.OpenAPIService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OpenAPIController {

	private final OpenAPIService openAPIService;

	@GetMapping("/open/getApiRecipe/{startIndex}/{endIndex}")
	public ResponseEntity<List<OpenAPIDto>> getOpenAPIRecipeList(@PathVariable("startIndex")
	String startIndex, @PathVariable("endIndex")
	String endIndex) {
		List<OpenAPIDto> openAPIRecipeList = openAPIService.getSlideList(startIndex, endIndex);
		return ResponseEntity.ok(openAPIRecipeList);
	}

	@GetMapping("/open/getApiRecipeSortDesc/{startIndex}/{endIndex}")
	public ResponseEntity<List<OpenAPIDto>> getOpenAPIRecipeSortDescList(@PathVariable("startIndex")
	String startIndex, @PathVariable("endIndex")
	String endIndex) {
		List<OpenAPIDto> openAPIRecipeList = openAPIService.getOpenAPIRecipeSortDescList(startIndex, endIndex);
		return ResponseEntity.ok(openAPIRecipeList);
	}

	@GetMapping("/open/getApiRecipeDe/{rcpName}")
	public ResponseEntity<List<OpenAPIDto>> getOpenAPIRecipeDetail(@PathVariable("rcpName")
	String rcpName) {
		List<OpenAPIDto> openAPIRecipeDetail = openAPIService.getOpenAPIRecipeDetail(rcpName);
		return ResponseEntity.ok(openAPIRecipeDetail);
	}
}
