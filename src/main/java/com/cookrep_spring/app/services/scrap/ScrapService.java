package com.cookrep_spring.app.services.scrap;

import com.cookrep_spring.app.models.recipe.Recipe;
import com.cookrep_spring.app.models.scrap.Scrap;
import com.cookrep_spring.app.models.scrap.ScrapPK;
import com.cookrep_spring.app.models.user.User;
import com.cookrep_spring.app.repositories.recipe.RecipeRepository;
import com.cookrep_spring.app.repositories.scrap.ScrapRepository;
import com.cookrep_spring.app.repositories.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class ScrapService {
    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    // 스크랩 등록
    public Scrap scrapRecipe(String userId, String recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("레시피를 찾을 수 없습니다."));

        Scrap scrap = Scrap.builder()
                .id(new ScrapPK(userId, recipeId))
                .user(user)
                .recipe(recipe)
                .build();

        return scrapRepository.save(scrap);
    }

    // 스크랩 취소
    public void cancelScrap(String userId, String recipeId) {
        Scrap scrap = scrapRepository.findById(new ScrapPK(userId, recipeId))
                .orElseThrow(() -> new EntityNotFoundException("스크랩이 존재하지 않습니다."));
        scrapRepository.delete(scrap);
    }

    // 스크랩 여부 확인
    public boolean isScrapped(String userId, String recipeId) {
        return scrapRepository.existsById(new ScrapPK(userId, recipeId));
    }
}