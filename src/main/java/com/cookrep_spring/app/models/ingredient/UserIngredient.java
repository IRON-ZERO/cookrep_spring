package com.cookrep_spring.app.models.ingredient;

import com.cookrep_spring.app.models.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "useringredient")
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserIngredient {

    @EmbeddedId
    private UserIngredientPK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_useringredient_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ingredientId")
    @JoinColumn(name = "ingredient_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_useringredient_ingredient"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Ingredient ingredient;
}