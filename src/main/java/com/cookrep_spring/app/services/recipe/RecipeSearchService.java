package com.cookrep_spring.app.services.recipe;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cookrep_spring.app.dto.recipe.response.RecipeSearchResultDto;
import com.cookrep_spring.app.models.recipe.Recipe;
import com.cookrep_spring.app.repositories.recipe.RecipeRepository;
import com.cookrep_spring.app.utils.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeSearchService {
	private final RecipeRepository recipeRepo;
	private final S3Service s3Service;

	@Transactional(readOnly = true)
	public List<RecipeSearchResultDto> getRecipesAsName(String title) {
		List<Recipe> recipe;
		if (title == null || title.isBlank()) {
			recipe = recipeRepo.findTop20ByOrderByCreatedAtDesc();
		} else {
			recipe = recipeRepo.findByTitleContaining(title);
		}
		return recipe.stream().map(this::searchBuilder).toList();
	}

	private RecipeSearchResultDto searchBuilder(Recipe r) {
		String thumbnailKey = r.getThumbnailImageUrl();
		String thumbnailUrl = null;
		if (thumbnailKey != null && !thumbnailKey.isEmpty()) {
			thumbnailUrl = s3Service.generateDownloadPresignedUrls(List.of(thumbnailKey))
				.get(0)
				.get("downloadUrl");
		}
		return RecipeSearchResultDto.builder()
			.recipeId(r.getRecipeId())
			.title(r.getTitle())
			.thumbnailImageUrl(thumbnailUrl)
			.views(r.getViews())
			.peopleCount(r.getPeopleCount())
			.prepTime(r.getPrepTime())
			.cookTime(r.getCookTime())
			.likesCount(r.getLikesCount())
			.kcal(r.getKcal())
			.build();
	}
}