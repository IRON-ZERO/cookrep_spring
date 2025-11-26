package com.cookrep_spring.app.dto.recipe.response;

import com.cookrep_spring.app.models.recipe.Recipe;
import lombok.Builder;
import lombok.Getter;


// 제목
// 메인 사진
// 조회수
// 좋아요 수

@Getter
@Builder
public class RecipeListResponse {
    private String recipeId;
    private String title;
    private String thumbnailImageUrl;
    private int views;
    private int likeCount;

    public static RecipeListResponse from(Recipe recipe) {
        return RecipeListResponse.builder()
                .recipeId(recipe.getRecipeId())
                .title(recipe.getTitle())
                .thumbnailImageUrl(recipe.getThumbnailImageUrl())
                .views(recipe.getViews())
                .likeCount(recipe.getLikesCount())
                .build();
    }
}
