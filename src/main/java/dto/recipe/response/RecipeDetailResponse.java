package dto.recipe.response;

import com.cookrep_spring.app.models.Recipe;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
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
    private String authorNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> ingredients;
    private List<String> steps;

    public static RecipeDetailResponse from(Recipe recipe, List<String> ingredients, List<String> steps, String authorNickname) {
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
                .build();
    }
}

