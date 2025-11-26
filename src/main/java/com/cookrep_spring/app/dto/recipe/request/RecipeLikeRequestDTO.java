package com.cookrep_spring.app.dto.recipe.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeLikeRequestDTO {
    private String recipeId;
    private String userId;
}
