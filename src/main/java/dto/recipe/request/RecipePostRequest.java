package dto.recipe.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipePostRequest {
    private String title;
    private String thumbnailImageUrl;
    private int peopleCount;
    private int prepTime;
    private int cookTime;
    private List<StepDto> steps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StepDto {
        private int stepOrder;
        private String contents;
        private String imageUrl;
    }
}
