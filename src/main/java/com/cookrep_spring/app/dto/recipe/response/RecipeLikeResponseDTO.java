package com.cookrep_spring.app.dto.recipe.response;

import com.cookrep_spring.app.models.recipe.RecipeLike;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class RecipeLikeResponseDTO {
    private String recipeId;
    private String userId;
    private boolean liked; //좋아요 상태
    private int likeCount; // 좋아요 갯수
    private String status; // "success" / "error"
    private String message;

    public static RecipeLikeResponseDTO from(RecipeLike recipeLike, int likeCount){
        return RecipeLikeResponseDTO
                .builder()
                .recipeId(recipeLike.getRecipe().getRecipeId())
                .userId(recipeLike.getUser().getUserId())
                .liked(true)
                .likeCount(likeCount)
                .build();
    }
}
