package com.cookrep_spring.app.repositories.recipe;

import com.cookrep_spring.app.dto.recipe.response.RecipeListResponseDTO;
import com.cookrep_spring.app.models.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, String> {
    // 레시피 조회(입력 받은 userId로 사용자가 작성한 레시피만 받아오기)
    List<Recipe> findByUser_UserIdOrderByCreatedAtDesc(String userId);
}