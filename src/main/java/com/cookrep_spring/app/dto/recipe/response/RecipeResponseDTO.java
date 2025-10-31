package com.cookrep_spring.app.dto.recipe.response;

import com.cookrep_spring.app.models.Recipe;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeResponseDTO {

    private String recipeId;
    private String title;
    private String thumbnailImageUrl;
    private int views;
    private int peopleCount;
    private int prepTime;
    private int cookTime;
    private int likesCount;
    private int kcal;

    public static RecipeResponseDTO of(Recipe recipe, String presignedUrl) {
        return RecipeResponseDTO.builder()
                                .recipeId(recipe.getRecipeId())
                                .title(recipe.getTitle())
                                .thumbnailImageUrl(presignedUrl)
                                .views(recipe.getViews())
                                .peopleCount(recipe.getPeopleCount())
                                .prepTime(recipe.getPrepTime())
                                .cookTime(recipe.getCookTime())
                                .likesCount(recipe.getLikesCount())
                                .kcal(recipe.getKcal())
                                .build();
    }
}
