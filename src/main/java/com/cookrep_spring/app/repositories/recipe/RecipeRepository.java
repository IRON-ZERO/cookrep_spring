package com.cookrep_spring.app.repositories.recipe;

import com.cookrep_spring.app.models.recipe.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cookrep_spring.app.models.user.User;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, String> {
    List<Recipe> findByUser(User user);
    // 레시피 조회(입력 받은 userId로 사용자가 작성한 레시피만 받아오기)
    List<Recipe> findByUser_UserIdOrderByCreatedAtDesc(String userId);
}