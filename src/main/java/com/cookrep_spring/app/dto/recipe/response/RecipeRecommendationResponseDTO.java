package com.cookrep_spring.app.dto.recipe.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RecipeRecommendationResponseDTO {

    private RecipeListResponseDTO recipe;
    private Long matchCount;
    private boolean scrapped;

    public static RecipeRecommendationResponseDTO of(RecipeListResponseDTO recipe, Long matchCount, boolean isScrapped){
        return RecipeRecommendationResponseDTO.builder()
                                              .recipe(recipe)
                                              .matchCount(matchCount)
                                              .scrapped(isScrapped)
                                              .build();
    }
}