package com.cookrep_spring.app.dto.recipe.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipePostRequest {
    private String userId;
    private String title;
    private String thumbnailImageUrl;
    private int peopleCount;
    private int prepTime;
    private int cookTime;
    private Integer kcal;
    private List<StepDto> steps;
    private List<IngredientRequest> ingredients; // 이름+수량을 함께 받기


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StepDto {
        private Integer stepOrder;
        private String contents;
        private String imageUrl;
    }


    @Data
    public static class IngredientRequest {
        private String name;   // 재료 이름
        private String count;  // 재료 수량
    }
}
