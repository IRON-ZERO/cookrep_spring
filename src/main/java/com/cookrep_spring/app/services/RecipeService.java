package com.cookrep_spring.app.services;

import com.cookrep_spring.app.models.Recipe;
import com.cookrep_spring.app.models.RecipeSteps;
import com.cookrep_spring.app.models.user.User;
import com.cookrep_spring.app.repositories.recipe.RecipeRepository;
import com.cookrep_spring.app.repositories.recipe.RecipeStepsRepository;
import com.cookrep_spring.app.repositories.user.UserRepository;
import com.cookrep_spring.app.utils.S3Service;
import dto.recipe.request.RecipePostRequest;
import dto.recipe.response.RecipeDetailResponse;
import dto.recipe.response.RecipeUpdateResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

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
                .orElseThrow(() -> new RuntimeException("Recipe not Found"));

        // 2. Ingredients 문자열 리스트 (예: ingredient.getName())
        // List<String> ingredients = recipe.getIngredients() != null ?
        //        recipe.getIngredients().stream()
        //                .map(ingredient -> ingredient.getName())
        //                .collect(Collectors.toList()) : List.of();

        // 3. 메인 이미지 다운로드용 Presigned URL 생성
        String thumbnailKey = recipe.getThumbnailImageUrl();
        String thumbnailUrl = null;
        if (thumbnailKey != null && !thumbnailKey.isEmpty()) {
            thumbnailUrl = s3Service.generateDownloadPresignedUrls(List.of(thumbnailKey))
                    .get(0)
                    .get("downloadUrl");
        }

        // 4. Step 조회
        List<RecipeSteps> steps = recipeStepsRepository.findByRecipe_RecipeIdOrderByStepOrderAsc(recipeId);

        // 5. Step 내용 + 다운로드용 Presigned URL 적용
        List<String> stepDescriptions = steps.stream()
                .map(step -> {
                    String imageKey = step.getImageUrl();
                    String imageUrl = null;
                    if (imageKey != null && !imageKey.isEmpty()) {
                        imageUrl = s3Service.generateDownloadPresignedUrls(List.of(imageKey))
                                .get(0)
                                .get("downloadUrl");
                    }
                    return step.getStepOrder() + ". " + step.getContents() +
                            (imageUrl != null ? " (이미지: " + imageUrl + ")" : "");
                })
                .collect(Collectors.toList());

        // 6. 작성자 닉네임
        String authorNickname = recipe.getUser() != null ? recipe.getUser().getNickname() : "unknown";

        // 7. DTO 변환
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

        // 3. S3 삭제 먼저 시도 (DB 트랜잭션에 영향을 주지 않음)
        try {
            // 메인 이미지 S3 삭제
            if (recipe.getThumbnailImageUrl() != null && !recipe.getThumbnailImageUrl().isEmpty()) {
                s3Service.deleteObject(recipe.getThumbnailImageUrl());
            }

            // 단계별 이미지 S3 삭제
            for (RecipeSteps step : steps) {
                if (step.getImageUrl() != null && !step.getImageUrl().isEmpty()) {
                    s3Service.deleteObject(step.getImageUrl());
                }
            }
        } catch (Exception e) {
            // S3 삭제 실패 시 DB 삭제는 절대 진행하지 않고 바로 예외 발생
            e.printStackTrace();
            throw new RuntimeException("S3 삭제 실패로 레시피 삭제 중단: " + recipeId, e);
        }

        // 4. DB 삭제 (S3 삭제 성공 시만 진행)
        recipeStepsRepository.deleteAll(steps); // CascadeType.REMOVE 사용 중이면 이 부분 생략 가능
        recipeRepository.delete(recipe);

        return true;
    }




}
