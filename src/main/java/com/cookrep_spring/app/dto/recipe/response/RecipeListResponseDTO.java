package com.cookrep_spring.app.dto.recipe.response;

import com.cookrep_spring.app.models.recipe.Recipe;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeListResponseDTO {

    private String recipeId;
    private String title;
    private String thumbnailImageUrl;
    private String cookLevel;
    private int views;
    private int peopleCount;
    private int prepTime;
    private int cookTime;
    private int likesCount;
    private int kcal;
    private boolean scrapped;

    public static RecipeListResponseDTO of(Recipe recipe, boolean isScrapped) {
        return RecipeListResponseDTO.builder()
                                    .recipeId(recipe.getRecipeId())
                                    .title(recipe.getTitle())
                                    .thumbnailImageUrl(recipe.getThumbnailImageUrl())
                                    .views(recipe.getViews())
                                    .peopleCount(recipe.getPeopleCount())
                                    .prepTime(recipe.getPrepTime())
                                    .cookTime(recipe.getCookTime())
                                    .likesCount(recipe.getLikesCount())
                                    .kcal(recipe.getKcal())
                                    .scrapped(isScrapped)
                                    .cookLevel(getCookLevel(recipe))
                                    .build();
    }

    public static RecipeListResponseDTO from(Recipe recipe) {
        return RecipeListResponseDTO.builder()
                .recipeId(recipe.getRecipeId())
                .title(recipe.getTitle())
                .thumbnailImageUrl(recipe.getThumbnailImageUrl())
                .views(recipe.getViews())
                .peopleCount(recipe.getPeopleCount())
                .prepTime(recipe.getPrepTime())
                .cookTime(recipe.getCookTime())
                .likesCount(recipe.getLikesCount())
                .kcal(recipe.getKcal())
                .cookLevel(getCookLevel(recipe))
                .build();
    }

    public static String getCookLevel(Recipe recipe) {
        int pTime = recipe.getPrepTime();
        int cTime = recipe.getCookTime();
        String easy = "EASY", normal = "NORMAL", hard = "HARD";
        boolean easyCoast = (pTime < 40 && cTime < 40);
        boolean hardCoast = (pTime > 50 && cTime > 60) || cTime > 100;
        if (easyCoast) {
            return easy;
        } else if (hardCoast) {
            return hard;
        }
        return normal;
    }
}