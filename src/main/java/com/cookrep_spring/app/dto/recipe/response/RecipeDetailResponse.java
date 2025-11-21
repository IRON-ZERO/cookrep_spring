package com.cookrep_spring.app.dto.recipe.response;

import com.cookrep_spring.app.dto.ingredient.response.IngredientRecipeResponse;
import com.cookrep_spring.app.models.recipe.Recipe;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class RecipeDetailResponse {
    private String recipeId;
    private String title;
    private String thumbnailImageUrl;
    private int views;
    private int like;
    private int kcal;
    private int peopleCount;
    private int prepTime;
    private int cookTime;
    private boolean isOwner; // 작성자 여부
    private boolean liked; // 로그인 사용자의 좋아요 여부
    private String authorNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<IngredientRecipeResponse> ingredients;

    // 수정: Map 대신 StepResponse DTO 사용
    private List<StepResponse> steps;

    public static RecipeDetailResponse from(
            Recipe recipe,
            List<IngredientRecipeResponse> ingredients,
            List<StepResponse> steps,
            String authorNickname,
            String currentUserId //로그인 사용자 ID
    ) {
        return RecipeDetailResponse.builder()
                .recipeId(recipe.getRecipeId())
                .title(recipe.getTitle())
                .thumbnailImageUrl(recipe.getThumbnailImageUrl())
                .views(recipe.getViews())
                .like(recipe.getLikesCount())
                .kcal(recipe.getKcal() != null ? recipe.getKcal() : 0)
                .peopleCount(recipe.getPeopleCount())
                .prepTime(recipe.getPrepTime())
                .cookTime(recipe.getCookTime())
                .authorNickname(authorNickname)
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .ingredients(ingredients)
                .steps(steps)
                .isOwner(currentUserId != null && recipe.getUser().getUserId().equals(currentUserId))
                .build();
    }
}



