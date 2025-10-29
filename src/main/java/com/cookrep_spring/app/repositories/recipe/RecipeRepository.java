package com.cookrep_spring.app.repositories.recipe;

import com.cookrep_spring.app.models.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, String> {
}
