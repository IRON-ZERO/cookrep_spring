package com.cookrep_spring.app.services.ingredient;

import com.cookrep_spring.app.models.ingredient.Ingredient;
import com.cookrep_spring.app.models.ingredient.UserIngredient;
import com.cookrep_spring.app.models.ingredient.UserIngredientPK;
import com.cookrep_spring.app.models.user.User;
import com.cookrep_spring.app.repositories.ingredient.IngredientRepository;
import com.cookrep_spring.app.repositories.ingredient.UserIngredientRepository;
import com.cookrep_spring.app.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserIngredientService {
    private final UserIngredientRepository userIngredientRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    // 유저 냉장고에 재료 추가
    // 아래 조건에 맞추어 동시에 재료 테이블에도 추가됨.
    // Ingredient에 해당 재료가 없다면 Ingredient에도 추가.
    // 추가된 Ingredient나 기존에 있다면 해당 Ingredient의 Id를 가져온다.
    @Transactional
    public List<Ingredient> addIngredients(String userId, String[] ingredientNames) {
        // 재료들 각각의 for문
        for (String ingredientName : ingredientNames) {
            // [1] 재료 존재 여부 확인
            Ingredient ingredient = ingredientRepository.findByName(ingredientName)
                .orElseGet(() -> {
                    Ingredient newIngredient = Ingredient.builder()
                                                         .name(ingredientName)
                                                         .build();
                    return ingredientRepository.save(newIngredient);
                });
            // [2] 유저 냉장고에 있는지 확인
            boolean exists = userIngredientRepository
                .existsByUser_UserIdAndIngredient_IngredientId(userId, ingredient.getIngredientId());
            if (exists) continue;

            // [3] 관계 추가
            User user = userRepository.getReferenceById(userId);
            UserIngredientPK id = new UserIngredientPK(userId, ingredient.getIngredientId());
            UserIngredient userIngredient = UserIngredient.builder()
                .id(id)
                .user(user)
                .ingredient(ingredient)
                .build();

            userIngredientRepository.save(userIngredient);
        }
        return userIngredientRepository.findIngredientsByUser_UserId(userId);
    }

    // 유저 냉장고에 재료 삭제
    public boolean deleteByUserIdAndIngredientId(String userId, int ingredientId){
        return userIngredientRepository
            .findByUser_UserIdAndIngredient_IngredientId(userId, ingredientId)
            .map(userIngredient -> {
                userIngredientRepository.delete(userIngredient);
                return true;
            })
            .orElse(false);
    }
    // 유저 냉장고의 재료 검색(findAll)
    public Optional<List<Ingredient>> findAllByUserId(String userID){
        return userRepository.findById(userID)
                             .map(user -> userIngredientRepository
                                 .findIngredientsByUser_UserId(user.getUserId()));
    }

    // (레시피 서비스로 옮겨야 함) 유저 냉장고의 재료로 레시피 검색
}
