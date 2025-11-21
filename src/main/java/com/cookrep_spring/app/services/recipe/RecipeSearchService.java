package com.cookrep_spring.app.services.recipe;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cookrep_spring.app.dto.recipe.response.RecipeSearchResultDto;
import com.cookrep_spring.app.models.recipe.Recipe;
import com.cookrep_spring.app.repositories.recipe.RecipeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeSearchService {
	private final RecipeRepository recipeRepo;

	@Transactional(readOnly = true)
	public List<RecipeSearchResultDto> getRecipesAsName(String title) {
		List<Recipe> recipe = recipeRepo.findByTitleContaining(title);
		return recipe.stream().map(this::searchBuilder).toList();
	}

	private RecipeSearchResultDto searchBuilder(Recipe r) {
		return RecipeSearchResultDto.builder()
			.recipeId(r.getRecipeId())
			.title(r.getTitle())
			.thumbnailImageUrl(r.getThumbnailImageUrl())
			.views(r.getViews())
			.peopleCount(r.getPeopleCount())
			.prepTime(r.getPrepTime())
			.likesCount(r.getLikesCount())
			.kcal(r.getKcal())
			.build();
	}
}