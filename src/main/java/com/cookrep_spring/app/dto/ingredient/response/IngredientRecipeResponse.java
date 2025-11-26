package com.cookrep_spring.app.dto.ingredient.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class IngredientRecipeResponse {
    private String name;
    private String count;
}