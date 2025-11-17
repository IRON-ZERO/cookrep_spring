package com.cookrep_spring.app.repositories.scrap;

import com.cookrep_spring.app.models.Recipe;
import com.cookrep_spring.app.models.scrap.Scrap;
import com.cookrep_spring.app.models.scrap.ScrapPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScrapRepository extends JpaRepository<Scrap, ScrapPK> {
    @Query("SELECT s.recipe.recipeId FROM Scrap s WHERE s.user.userId = :userId")
    List<String> findRecipeIdsByUserId(@Param("userId") String userId);

    @Query("SELECT s.recipe FROM Scrap as s WHERE s.user.userId = :userId")
    List<Recipe> findRecipesByUserId(@Param("userId") String userId);
}
