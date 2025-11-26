package com.cookrep_spring.app.dto.recipe.response;

import com.cookrep_spring.app.models.recipe.Recipe;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class RecipeUpdateResponse {
    private String recipeId;
    private String status; // "success" / "error"
    private String title;
    private int peopleCount;
    private int prepTime;
    private int cookTime;
    private Integer kcal;
    private String thumbnailImageUrl;
    private LocalDateTime updatedAt;
    private List<String> ingredientNames;

    public static RecipeUpdateResponse from(Recipe recipe){
        return RecipeUpdateResponse
                .builder()
                .recipeId(recipe.getRecipeId())
                .title(recipe.getTitle())
                .peopleCount(recipe.getPeopleCount())
                .prepTime(recipe.getPrepTime())
                .cookTime(recipe.getCookTime())
                .kcal(recipe.getKcal())
                .thumbnailImageUrl(recipe.getThumbnailImageUrl())
                .updatedAt(recipe.getUpdatedAt())
                .ingredientNames(
                        recipe.getRecipeIngredients() != null
                                ? recipe.getRecipeIngredients().stream()
                                .map(ri -> ri.getIngredient().getName())
                                .toList()
                                : List.of()
                )
                .build();
    }
}

