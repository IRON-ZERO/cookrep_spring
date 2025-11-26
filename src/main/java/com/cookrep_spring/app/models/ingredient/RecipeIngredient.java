package com.cookrep_spring.app.models.ingredient;

import com.cookrep_spring.app.models.recipe.Recipe;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeIngredient {

    @EmbeddedId
    private RecipeIngredientPK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recipeId")
    @JoinColumn(name = "recipe_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recipeingredient_recipe"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ingredientId")
    @JoinColumn(name = "ingredient_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_recipeingredient_ingredient"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Ingredient ingredient;

    @Column(length = 20)
    private String count;
}