package com.cookrep_spring.app.repositories.ingredient;

import com.cookrep_spring.app.dto.recipe.response.RecipeMatchDTO;
import com.cookrep_spring.app.models.ingredient.RecipeIngredient;
import com.cookrep_spring.app.models.ingredient.RecipeIngredientPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, RecipeIngredientPK> {
    @Query("""
    SELECT new com.cookrep_spring.app.dto.recipe.response.RecipeMatchDTO(ri.recipe, COUNT(i.ingredientId))
    FROM RecipeIngredient ri
    JOIN ri.ingredient i
    WHERE i.name IN :ingredientNames
    GROUP BY ri.recipe.recipeId
    ORDER BY COUNT(i.ingredientId) DESC, ri.recipe.views DESC
""")
    List<RecipeMatchDTO> findRecipesWithMatchCount(@Param("ingredientNames") List<String> ingredientNames);
    List<RecipeIngredient> findByRecipe_RecipeId(String recipeId);


    @Modifying
    @Transactional
    @Query("DELETE FROM RecipeIngredient ri WHERE ri.recipe.recipeId = :recipeId AND ri.ingredient.name = :name")
    void deleteByRecipeIdAndIngredientName(@Param("recipeId") String recipeId, @Param("name") String name);


}