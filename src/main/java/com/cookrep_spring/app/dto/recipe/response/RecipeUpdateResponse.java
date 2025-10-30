package com.cookrep_spring.app.dto.recipe.response;

import com.cookrep_spring.app.models.Recipe;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

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
                .build();
    }
}

