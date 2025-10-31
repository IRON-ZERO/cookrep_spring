package com.cookrep_spring.app.repositories.ingredient;

import com.cookrep_spring.app.models.ingredient.Ingredient;
import com.cookrep_spring.app.models.ingredient.UserIngredient;
import com.cookrep_spring.app.models.ingredient.UserIngredientPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserIngredientRepository extends JpaRepository<UserIngredient, UserIngredientPK> {
    @Query("SELECT ui.ingredient FROM UserIngredient ui WHERE ui.user.userId = :userId")
    List<Ingredient> findIngredientsByUser_UserId(@Param("userId") String user_Id);

    Optional<UserIngredient> findByUser_UserIdAndIngredient_IngredientId(String userId, int ingredientId);

    boolean existsByUser_UserIdAndIngredient_IngredientId(String userId, Integer ingredientId);

    void deleteByUser_UserIdAndIngredient_IngredientId(String userID, int ingredientId);
}
