package com.cookrep_spring.app.security;

import com.cookrep_spring.app.repositories.recipe.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("recipeSecurity")
@RequiredArgsConstructor
public class RecipeSecurity {

    private final RecipeRepository recipeRepository;

    public boolean isOwner(String recipeId, Authentication authentication) {
        String loginUserId = ((CustomUserDetail) authentication.getPrincipal()).getUserId();
        return recipeRepository.findById(recipeId)
                .map(recipe -> recipe.getUser().getUserId().equals(loginUserId))
                .orElse(false);
    }
}
