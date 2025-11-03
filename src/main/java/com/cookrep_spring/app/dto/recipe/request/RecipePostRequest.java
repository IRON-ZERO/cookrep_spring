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
    private List<StepDto> steps;
    private String[] ingredientNames;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StepDto {
        private Integer stepOrder;
        private String contents;
        private String imageUrl;
    }
}
