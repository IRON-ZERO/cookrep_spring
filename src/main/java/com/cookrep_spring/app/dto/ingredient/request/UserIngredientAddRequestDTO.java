package com.cookrep_spring.app.dto.ingredient.request;

import lombok.Data;

@Data
public class UserIngredientAddRequestDTO {
    private String[] ingredientNames;
}