package com.cookrep_spring.app.dto.recipe.response;

import com.cookrep_spring.app.models.Recipe;
import lombok.AllArgsConstructor;
import lombok.Getter;

// DTO Projection용 DTO
@Getter
@AllArgsConstructor
public class RecipeMatchDTO {
    private Recipe recipe;
    private Long matchCount;
}