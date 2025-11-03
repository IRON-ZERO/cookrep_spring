package com.cookrep_spring.app.dto.recipe.request;

import lombok.Data;

import java.util.List;

@Data
public class RecipeSearchByIngredientsRequestDTO {
    private List<Integer> ingredientIds;
}