package com.cookrep_spring.app.services.ingredient;

import com.cookrep_spring.app.models.ingredient.Ingredient;
import com.cookrep_spring.app.repositories.ingredient.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    public List<String> findNamesByIds(List<Integer> ids) {
        return ingredientRepository.findIngredientsByIds(ids)
            .stream()
            .map(Ingredient::getName)
            .toList();
    }
}
