package com.cookrep_spring.app.repositories.recipe;

import com.cookrep_spring.app.models.recipe.RecipeSteps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeStepsRepository extends JpaRepository<RecipeSteps, Integer> {
    List<RecipeSteps> findByRecipe_RecipeIdOrderByStepOrderAsc(String recipeId);

}
