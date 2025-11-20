package com.cookrep_spring.app.repositories.recipe;

import com.cookrep_spring.app.models.recipe.RecipeLike;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface RecipeLikeRepository extends JpaRepository<RecipeLike, Long> {

    // 레시피에 좋아요 누른 사용자 전체 조회
    List<RecipeLike> findByRecipe_RecipeId(String recipeId);


    // 객체 자체 반환 (토글 구현용)
    Optional<RecipeLike> findByRecipe_RecipeIdAndUser_UserId(String recipeId, String userId);

    // 좋아요 등록 시 recipelike 테이블에 업데이트


}
