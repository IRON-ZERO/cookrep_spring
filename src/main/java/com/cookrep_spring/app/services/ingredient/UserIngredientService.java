package com.cookrep_spring.app.services.ingredient;

import com.cookrep_spring.app.dto.recipe.response.RecipeMatchDTO;
import com.cookrep_spring.app.dto.recipe.response.RecipeResponseDTO;
import com.cookrep_spring.app.models.Recipe;
import com.cookrep_spring.app.models.ingredient.Ingredient;
import com.cookrep_spring.app.models.ingredient.UserIngredient;
import com.cookrep_spring.app.models.ingredient.UserIngredientPK;
import com.cookrep_spring.app.repositories.ingredient.IngredientRepository;
import com.cookrep_spring.app.repositories.ingredient.RecipeIngredientRepository;
import com.cookrep_spring.app.repositories.ingredient.UserIngredientRepository;
import com.cookrep_spring.app.repositories.user.UserRepository;
//import com.cookrep_spring.app.utils.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserIngredientService {
    private final UserIngredientRepository userIngredientRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
//    private final S3Service presigner;

    // 유저 냉장고에 재료 추가
    // 아래 조건에 맞추어 동시에 재료 테이블에도 추가됨.
    // Ingredient에 해당 재료가 없다면 Ingredient에도 추가.
    // 추가된 Ingredient나 기존에 있다면 해당 Ingredient의 Id를 가져온다.
    @Transactional
    public List<Ingredient> addIngredients(String userId, String[] ingredientNames) {
        // 1️⃣ 이미 등록된 재료들 미리 조회
        List<Ingredient> existingIngredients = ingredientRepository.findByNameIn(List.of(ingredientNames));
        Set<String> existingNames = existingIngredients.stream()
                                                       .map(Ingredient::getName)
                                                       .collect(Collectors.toSet());

        // 2️⃣ DB에 없는 재료들만 새로 생성
        List<Ingredient> newIngredients = Arrays.stream(ingredientNames)
                                                .filter(name -> !existingNames.contains(name))
                                                .map(name -> Ingredient.builder().name(name).build())
                                                .toList();
        ingredientRepository.saveAll(newIngredients);

        // 3️⃣ 전체 재료 목록 = 기존 + 신규
        List<Ingredient> allIngredients = new ArrayList<>();
        allIngredients.addAll(existingIngredients);
        allIngredients.addAll(newIngredients);

        // 4️⃣ 유저 냉장고에 이미 등록된 재료 제외

        List<Integer> alreadyHasIds = userIngredientRepository.findIngredientsByUser_UserId(userId)
                                                              .stream()
                                                              .map(Ingredient::getIngredientId)
                                                              .toList();

        List<UserIngredient> newUserIngredients = allIngredients.stream()
                                                                .filter(i -> !alreadyHasIds.contains(i.getIngredientId()))
                                                                .map(i -> UserIngredient.builder()
                                                                                        .id(new UserIngredientPK(userId, i.getIngredientId()))
                                                                                        .user(userRepository.getReferenceById(userId))
                                                                                        .ingredient(i)
                                                                                        .build())
                                                                .toList();

        userIngredientRepository.saveAll(newUserIngredients);

        // 5️⃣ 결과 반환
        return allIngredients;
    }

    // 유저 냉장고에 재료 삭제
    @Transactional
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

    // TODO: 유저 냉장고의 재료로 레시피 검색 기능을 레시피 서비스로 이동
    /**
     * 냉장고 재료 기반 레시피 추천
     * - key: RecipeResponseDTO
     * - value: 일치 재료 수
     */
    public Map<RecipeResponseDTO, Integer> recommendWithMatchCount(List<String> ingredientNames) {
        Map<RecipeResponseDTO, Integer> result = new LinkedHashMap<>();

        if (ingredientNames == null || ingredientNames.isEmpty()) return result;

        List<RecipeMatchDTO> queryResult = recipeIngredientRepository.findRecipesWithMatchCount(ingredientNames);

        for (RecipeMatchDTO recipeDTO : queryResult) {
            Recipe recipe = recipeDTO.getRecipe();
            Long matchCount = recipeDTO.getMatchCount();

            // Presigned URL 생성 (기존 JSP 로직 그대로)
            String url = recipe.getThumbnailImageUrl();
            if (url != null && !url.startsWith("https://")) {
//                url = presigner.generatePresignedUrls(url);
            }

            RecipeResponseDTO dto = RecipeResponseDTO.of(recipe, url);
            result.put(dto, matchCount.intValue());
        }
        return result;
    }

}
