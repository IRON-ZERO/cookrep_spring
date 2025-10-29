package com.cookrep_spring.app.services;

import com.cookrep_spring.app.config.AwsS3Config;
import com.cookrep_spring.app.models.Recipe;
import com.cookrep_spring.app.models.RecipeSteps;
import com.cookrep_spring.app.models.user.User;
import com.cookrep_spring.app.repositories.recipe.RecipeRepository;
import com.cookrep_spring.app.repositories.recipe.RecipeStepsRepository;
import com.cookrep_spring.app.repositories.user.UserRepository;
import dto.recipe.request.RecipePostRequest;
import dto.recipe.response.RecipeDetailResponse;
import dto.recipe.response.RecipeUpdateResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeStepsRepository recipeStepsRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private S3Client s3Client;

    @Transactional
    public RecipeUpdateResponse saveRecipe(String userId, RecipePostRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String recipeId = UUID.randomUUID().toString();

        Recipe recipe = Recipe.builder()
                .recipeId(recipeId)
                .user(user)
                .title(dto.getTitle())
                .thumbnailImageUrl(dto.getThumbnailImageUrl())
                .peopleCount(dto.getPeopleCount())
                .prepTime(dto.getPrepTime())
                .cookTime(dto.getCookTime())
                .build();

        recipeRepository.save(recipe);

        // Step 저장
        List<RecipeSteps> steps = dto.getSteps().stream()
                .map(s -> RecipeSteps.builder()
                        .stepOrder(s.getStepOrder())
                        .contents(s.getContents())
                        .imageUrl(s.getImageUrl())
                        .recipe(recipe)  // FK 설정
                        .build())
                .collect(Collectors.toList());

        recipeStepsRepository.saveAll(steps);

        return RecipeUpdateResponse.from(recipe)
                .toBuilder()
                .status("success")
                .build();
    }

    @Transactional(readOnly = true)
    public RecipeDetailResponse getRecipeDetail(String recipeId){
        // 1. 레시피 조회
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(()-> new RuntimeException("Recipe not Found"));

        // 2. Ingredients 문자열 리스트 (예: ingredient.getName())
//        List<String> ingredients = recipe.getIngredients() != null ?
//                recipe.getIngredients().stream()
//                        .map(ingredient -> ingredient.getName())
//                        .collect(Collectors.toList()) : List.of();

        // 메인 이미지 Presigned URL 생성
        String thumbnailKey = recipe.getThumbnailImageUrl();
        String thumbnailUrl = null;
        if (thumbnailKey != null && !thumbnailKey.isEmpty()) {
            thumbnailUrl = s3Service.generatePresignedUrls(List.of(thumbnailKey))
                    .get(0)
                    .get("uploadUrl");
        }

        // 2. Step 조회
        List<RecipeSteps> steps = recipeStepsRepository.findByRecipe_RecipeIdOrderByStepOrderAsc(recipeId);


        // 3. Step 내용 + Presigned URL 적용
        List<String> stepDescriptions = steps.stream()
                .map(step -> {
                    String imageUrl = step.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // S3Service에서 presigned URL 생성
                        imageUrl = s3Service.generatePresignedUrls(List.of(imageUrl))
                                .get(0)
                                .get("uploadUrl");
                    }
                    return step.getStepOrder() + ". " + step.getContents() +
                            (imageUrl != null ? " (이미지: " + imageUrl + ")" : "");
                })
                .collect(Collectors.toList());


        // 4. 작성자 닉네임
        String authorNickname = recipe.getUser() != null ? recipe.getUser().getNickname() : "unknown";

        // 5. DTO 변환
        return RecipeDetailResponse.from(
                recipe.toBuilder().thumbnailImageUrl(thumbnailUrl).build(),
                List.of(), // Ingredients 현재 미구현
                stepDescriptions,
                authorNickname
        );
    }

    @Transactional
    public boolean deleteRecipe(String recipeId) {
        // 1. 레시피 조회
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not Found"));

        // 2. 단계별 이미지 조회
        List<RecipeSteps> steps = recipeStepsRepository.findByRecipe_RecipeIdOrderByStepOrderAsc(recipeId);

        try {
            // 3. 메인 이미지 S3 삭제
            if (recipe.getThumbnailImageUrl() != null && !recipe.getThumbnailImageUrl().isEmpty()) {
                s3Service.deleteObject(recipe.getThumbnailImageUrl());
            }

            // 4. 단계별 이미지 S3 삭제
            for (RecipeSteps step : steps) {
                if (step.getImageUrl() != null && !step.getImageUrl().isEmpty()) {
                    s3Service.deleteObject(step.getImageUrl());
                }
            }

            // 5. DB에서 레시피 및 단계 삭제
            recipeStepsRepository.deleteAll(steps);
            recipeRepository.delete(recipe);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("레시피 삭제 실패: " + recipeId, e);
        }
    }



}
