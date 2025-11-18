package com.cookrep_spring.app.dto.ingredient.response;

import com.cookrep_spring.app.models.ingredient.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserIngredientAddResponseDTO {
    private int ingredientId;
    private String name;
    private LocalDateTime createdAt;

    public static UserIngredientAddResponseDTO from(Ingredient ingredient) {
        return UserIngredientAddResponseDTO.builder()
                                        .ingredientId(ingredient.getIngredientId())
                                        .name(ingredient.getName())
                                        .createdAt(ingredient.getCreatedAt())
                                        .build();
    }

}