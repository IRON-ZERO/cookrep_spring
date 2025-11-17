package com.cookrep_spring.app.repositories.ingredient;

import com.cookrep_spring.app.models.ingredient.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
    Optional<Ingredient> findByName(String name);

    List<Ingredient> findByNameIn(List<String> names);

    @Query("SELECT i FROM Ingredient i WHERE i.ingredientId IN :ingredientIds")
    List<Ingredient> findIngredientsByIds(@Param("ingredientIds") List<Integer> ingredientIds);

}
