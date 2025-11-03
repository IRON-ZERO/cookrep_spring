package com.cookrep_spring.app.services.recipe;

import com.cookrep_spring.app.dto.recipe.response.RecipeListResponse;
import com.cookrep_spring.app.dto.recipe.response.StepResponse;
import com.cookrep_spring.app.models.ingredient.Ingredient;
import com.cookrep_spring.app.models.ingredient.RecipeIngredient;
import com.cookrep_spring.app.models.ingredient.RecipeIngredientPK;
import com.cookrep_spring.app.models.recipe.Recipe;
import com.cookrep_spring.app.models.recipe.RecipeSteps;
import com.cookrep_spring.app.models.user.User;
import com.cookrep_spring.app.repositories.ingredient.IngredientRepository;
import com.cookrep_spring.app.repositories.ingredient.RecipeIngredientRepository;
import com.cookrep_spring.app.repositories.recipe.RecipeRepository;
import com.cookrep_spring.app.repositories.recipe.RecipeStepsRepository;
import com.cookrep_spring.app.repositories.user.UserRepository;
import com.cookrep_spring.app.utils.S3Service;
import com.cookrep_spring.app.dto.recipe.request.RecipePostRequest;
import com.cookrep_spring.app.dto.recipe.response.RecipeDetailResponse;
import com.cookrep_spring.app.dto.recipe.response.RecipeUpdateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeStepsRepository recipeStepsRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private S3Client s3Client;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

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

        // Ingredient 저장
        if (dto.getIngredientNames() != null && dto.getIngredientNames().length > 0) {
            for (String name : dto.getIngredientNames()) {
                // 이미 존재하는 재료인지 확인
                Ingredient ingredient = ingredientRepository.findByName(name)
                        .orElseGet(() -> {
                            // 없으면 새로 생성
                            Ingredient newIngredient = Ingredient.builder()
                                    .name(name)
                                    .build();
                            return ingredientRepository.save(newIngredient);
                        });
                // 레시피-재료 연결
                RecipeIngredient ri = RecipeIngredient.builder()
                        .id(RecipeIngredientPK.builder()
                                .recipeId(recipe.getRecipeId())
                                .ingredientId(ingredient.getIngredientId())
                                .build())
                        .recipe(recipe)
                        .ingredient(ingredient)
                        .count("2개") // 필요에 따라 단위 입력
                        .build();

                recipeIngredientRepository.save(ri);
            }
        }

        return RecipeUpdateResponse.from(recipe)
                .toBuilder()
                .status("success")
                .build();
    }

    @Transactional
    public RecipeUpdateResponse updateRecipe(String recipeId, RecipePostRequest dto) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not Found"));

        // 기존 Step 및 이미지 정보 가져오기
        List<RecipeSteps> existingSteps = recipeStepsRepository.findByRecipe_RecipeIdOrderByStepOrderAsc(recipeId);

        // 기존 썸네일 값 따로 저장
        String oldThumbnail = recipe.getThumbnailImageUrl();

        // DB 업데이트
        recipe.setTitle(dto.getTitle());
        recipe.setThumbnailImageUrl(dto.getThumbnailImageUrl());
        recipe.setPeopleCount(dto.getPeopleCount());
        recipe.setPrepTime(dto.getPrepTime());
        recipe.setCookTime(dto.getCookTime());
        recipeRepository.save(recipe);

        // 기존 Step 삭제 후 새 Step 저장
        recipeStepsRepository.deleteAll(existingSteps);

        List<RecipeSteps> newSteps = dto.getSteps().stream()
                .map(s -> RecipeSteps.builder()
                        .stepOrder(s.getStepOrder())
                        .contents(s.getContents() != null ? s.getContents() : "")
                        .imageUrl(s.getImageUrl())
                        .recipe(recipe)
                        .build())
                .collect(Collectors.toList());
        recipeStepsRepository.saveAll(newSteps);

        // 삭제 대상 S3 URL 수집
        List<String> deleteKeys = new ArrayList<>();

        // 썸네일 변경 시
        if (oldThumbnail != null && !oldThumbnail.equals(dto.getThumbnailImageUrl())) {
            deleteKeys.add(oldThumbnail);
        }

        // Step 이미지 중 새 Step에 없는 기존 이미지 삭제
        Set<String> newStepImageUrls = newSteps.stream()
                .map(RecipeSteps::getImageUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        existingSteps.stream()
                .map(RecipeSteps::getImageUrl)
                .filter(Objects::nonNull)
                .filter(url -> !newStepImageUrls.contains(url))
                .forEach(deleteKeys::add);

        // S3 삭제는 트랜잭션 밖에서 처리
        deleteKeys.forEach(url -> {
            try {
                s3Service.deleteObject(url);
            } catch (Exception e) {
                log.warn("S3 삭제 실패: " + url, e);
            }
        });

        return RecipeUpdateResponse.from(recipe)
                .toBuilder()
                .status("success")
                .build();
    }


    @Transactional(readOnly = true)
    public List<RecipeListResponse> getRecipeList(String userId){
        // 1. 유저 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. 해당 유저가 작성한 레시피 목록 조회
        List<Recipe> recipes = recipeRepository.findByUser(user);

        // 3. 썸네일 URL 변환 및 Presigned URL 생성
        return recipes.stream()
                .map(recipe -> {
                    String thumbnailKey = recipe.getThumbnailImageUrl();
                    String thumbnailUrl = null;
                    if(thumbnailKey != null && !thumbnailKey.isEmpty()){
                        thumbnailUrl = s3Service.generateDownloadPresignedUrls(List.of(thumbnailKey))
                                .get(0)
                                .get("downloadUrl");
                    }

                    // 서명된 url로 교체하여 dto 변환
                    Recipe updateRecipe = recipe.toBuilder()
                            .thumbnailImageUrl(thumbnailUrl)
                            .build();

                    return RecipeListResponse.from(updateRecipe);
                })
                .toList();


    }

    @Transactional(readOnly = true)
    public RecipeDetailResponse getRecipeDetail(String recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not Found"));

        // 썸네일 Presigned URL
        String thumbnailKey = recipe.getThumbnailImageUrl();
        String thumbnailUrl = null;
        if (thumbnailKey != null && !thumbnailKey.isEmpty()) {
            thumbnailUrl = s3Service.generateDownloadPresignedUrls(List.of(thumbnailKey))
                    .get(0)
                    .get("downloadUrl");
        }

        // Step 목록 조회
        List<RecipeSteps> steps = recipeStepsRepository.findByRecipe_RecipeIdOrderByStepOrderAsc(recipeId);

        // Step 데이터를 객체 형태로 변환
        List<StepResponse> stepResponses = steps.stream()
                .map(step -> {
                    String imageKey = step.getImageUrl();
                    String imageUrl = null;
                    if (imageKey != null && !imageKey.isEmpty()) {
                        imageUrl = s3Service.generateDownloadPresignedUrls(List.of(imageKey))
                                .get(0)
                                .get("downloadUrl");
                    }
                    return StepResponse.builder()
                            .stepOrder(step.getStepOrder())
                            .contents(step.getContents() != null ? step.getContents() : "")
                            .imageUrl(imageUrl)
                            .build();
                })
                .sorted(Comparator.comparingInt(StepResponse::getStepOrder)) // stepOrder 기준 정렬
                .collect(Collectors.toList());


        String authorNickname = recipe.getUser() != null ? recipe.getUser().getNickname() : "unknown";

        return RecipeDetailResponse.from(
                recipe.toBuilder().thumbnailImageUrl(thumbnailUrl).build(),
                List.of(), // ingredients 생략
                stepResponses,
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
