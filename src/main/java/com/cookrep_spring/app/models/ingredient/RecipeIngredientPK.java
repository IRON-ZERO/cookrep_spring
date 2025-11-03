package com.cookrep_spring.app.models.ingredient;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class RecipeIngredientPK implements Serializable {
    private String recipeId;
    private Integer ingredientId;
}