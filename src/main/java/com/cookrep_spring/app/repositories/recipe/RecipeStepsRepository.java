package com.cookrep_spring.app.repositories.recipe;

import com.cookrep_spring.app.models.RecipeSteps;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepsRepository extends JpaRepository<RecipeSteps, Long> {
}
