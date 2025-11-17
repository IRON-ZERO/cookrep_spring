package com.cookrep_spring.app.models.ingredient;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class UserIngredientPK implements Serializable {
    // UserIngredient의 복합키로 사용할 클래스
    private String userId;
    private Integer ingredientId;
}
