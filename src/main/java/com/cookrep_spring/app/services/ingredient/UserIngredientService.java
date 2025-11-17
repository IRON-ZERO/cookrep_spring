package com.cookrep_spring.app.services.ingredient;

import com.cookrep_spring.app.dto.ingredient.response.UserIngredientAddResponseDTO;
import com.cookrep_spring.app.dto.ingredient.response.UserIngredientResponseDTO;
import com.cookrep_spring.app.dto.recipe.response.RecipeMatchDTO;
import com.cookrep_spring.app.dto.recipe.response.RecipeListResponseDTO;
import com.cookrep_spring.app.models.Recipe;
import com.cookrep_spring.app.models.ingredient.Ingredient;
import com.cookrep_spring.app.models.ingredient.UserIngredient;
import com.cookrep_spring.app.models.ingredient.UserIngredientPK;
import com.cookrep_spring.app.repositories.ingredient.IngredientRepository;
import com.cookrep_spring.app.repositories.ingredient.RecipeIngredientRepository;
import com.cookrep_spring.app.repositories.ingredient.UserIngredientRepository;
import com.cookrep_spring.app.repositories.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserIngredientService {

    private final UserIngredientRepository userIngredientRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    /**
     * 유저 냉장고에 재료 추가
     * - Ingredient 테이블에 없으면 자동 추가
     * - 이미 유저 냉장고에 등록된 재료는 중복 저장하지 않음
     */
    @Transactional
    public List<UserIngredientAddResponseDTO> addIngredients(String userId, String[] ingredientNames) {
        // 1️⃣ 유저 존재 여부 검증
        userRepository.findById(userId)
                      .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        // 2️⃣ 이미 등록된 재료들 미리 조회
        List<Ingredient> existingIngredients = ingredientRepository.findByNameIn(List.of(ingredientNames));
        Set<String> existingNames = existingIngredients.stream()
                                                       .map(Ingredient::getName)
                                                       .collect(Collectors.toSet());

        // 3️⃣ DB에 없는 재료들만 새로 생성
        List<Ingredient> newIngredients = Arrays.stream(ingredientNames)
                                                .filter(name -> !existingNames.contains(name))
                                                .map(name -> Ingredient.builder().name(name).build())
                                                .toList();
        ingredientRepository.saveAll(newIngredients);

        // 4️⃣ 전체 재료 목록 = 기존 + 신규
        List<Ingredient> allIngredients = new ArrayList<>();
        allIngredients.addAll(existingIngredients);
        allIngredients.addAll(newIngredients);

        // 5️⃣ 유저 냉장고에 이미 등록된 재료 제외
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

        return allIngredients.stream()
                             .map(UserIngredientAddResponseDTO::from)
                             .toList();
    }

    /**
     * 유저 냉장고에서 재료 삭제
     */
    @Transactional
    public void deleteByUserIdAndIngredientId(String userId, int ingredientId) {
        UserIngredient userIngredient = userIngredientRepository
            .findByUser_UserIdAndIngredient_IngredientId(userId, ingredientId)
            .orElseThrow(() -> new EntityNotFoundException("해당 재료가 냉장고에 없습니다."));
        userIngredientRepository.delete(userIngredient);
    }

    /**
     * 유저 냉장고의 재료 목록 조회
     */
    public List<UserIngredientResponseDTO> findAllByUserId(String userId) {
        // 유저 검증
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("유저를 찾을 수 없습니다.");
        }

        return userIngredientRepository.findIngredientsByUser_UserId(userId)
                                       .stream()
                                       .map(UserIngredientResponseDTO::from)
                                       .toList();
    }

    // TODO: 유저 냉장고의 재료로 레시피 검색 기능을 레시피 서비스로 이동
    /**
     * 냉장고 재료 기반 레시피 추천
     * - key: RecipeListResponseDTO
     * - value: 일치 재료 수
     */
    public Map<RecipeListResponseDTO, Integer> recommendWithMatchCount(List<String> ingredientNames) {
        Map<RecipeListResponseDTO, Integer> result = new LinkedHashMap<>();

        if (ingredientNames == null || ingredientNames.isEmpty()) {
            return result; // 빈 Map 반환
        }

        List<RecipeMatchDTO> queryResult = recipeIngredientRepository.findRecipesWithMatchCount(ingredientNames);

        for (RecipeMatchDTO recipeDTO : queryResult) {
            Recipe recipe = recipeDTO.getRecipe();
            Long matchCount = recipeDTO.getMatchCount();

            String url = recipe.getThumbnailImageUrl();
            // TODO: S3Service 구현 후 Presigned URL 생성 로직 추가 필요
            // if (url != null && !url.startsWith("https://")) {
            //     url = presigner.generatePresignedUrls(url);
            // }

            RecipeListResponseDTO dto = RecipeListResponseDTO.from(recipe);
            result.put(dto, matchCount.intValue());
        }

        return result;
    }
}
