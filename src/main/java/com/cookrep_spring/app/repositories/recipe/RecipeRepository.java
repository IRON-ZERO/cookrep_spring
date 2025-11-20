package com.cookrep_spring.app.repositories.recipe;

import com.cookrep_spring.app.models.recipe.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cookrep_spring.app.models.user.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, String> {
    List<Recipe> findByUser(User user);
    // 레시피 조회(입력 받은 userId로 사용자가 작성한 레시피만 받아오기)
    List<Recipe> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    @Modifying
    @Query("UPDATE Recipe r SET r.likesCount = r.likesCount + 1 WHERE r.recipeId = :id")
    void increaseLike(@Param("id") String id);

    @Modifying
    @Query("UPDATE Recipe r SET r.likesCount = r.likesCount - 1 WHERE r.recipeId = :id AND r.likesCount > 0")
    void decreaseLike(@Param("id") String id);

    @Query("SELECT r.likesCount FROM Recipe r WHERE r.recipeId = :id")
    Integer findLikesCountByRecipeId(@Param("id") String recipeId);

}