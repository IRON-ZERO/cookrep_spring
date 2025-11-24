package com.cookrep_spring.app.dto.recipe.response;

import lombok.Builder;

@Builder
public class RecipeViewsResponseDTO {
    int views;

    public static RecipeViewsResponseDTO of(int views){
        return RecipeViewsResponseDTO.builder().views(views).build();
    }
}
