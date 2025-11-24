package com.cookrep_spring.app.services.recipe;

import com.cookrep_spring.app.dto.ingredient.response.IngredientRecipeResponse;
import com.cookrep_spring.app.dto.recipe.response.*;
import com.cookrep_spring.app.models.ingredient.Ingredient;
import com.cookrep_spring.app.models.ingredient.RecipeIngredient;
import com.cookrep_spring.app.models.ingredient.RecipeIngredientPK;
import com.cookrep_spring.app.models.recipe.Recipe;
import com.cookrep_spring.app.models.recipe.RecipeSteps;
import com.cookrep_spring.app.models.user.User;
import com.cookrep_spring.app.repositories.ingredient.IngredientRepository;
import com.cookrep_spring.app.repositories.ingredient.RecipeIngredientRepository;
import com.cookrep_spring.app.repositories.recipe.RecipeLikeRepository;
import com.cookrep_spring.app.repositories.recipe.RecipeRepository;
import com.cookrep_spring.app.repositories.recipe.RecipeStepsRepository;
import com.cookrep_spring.app.repositories.user.UserRepository;
import com.cookrep_spring.app.security.CustomUserDetail;
import com.cookrep_spring.app.services.scrap.ScrapService;
import com.cookrep_spring.app.utils.S3Service;
import com.cookrep_spring.app.dto.recipe.request.RecipePostRequest;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final ScrapService scrapService;
    private final RecipeLikeRepository recipeLikeRepository;

    // Caffeine 캐시 주입
    private final Cache<String, Boolean> viewCache;

    // =============== upload =================
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
                .kcal(dto.getKcal())
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
        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            for (RecipePostRequest.IngredientRequest ingDto : dto.getIngredients()) {
                // 1. 재료 존재 여부 확인
                Ingredient ingredient = ingredientRepository.findByName(ingDto.getName())
                        .orElseGet(() -> {
                            Ingredient newIngredient = Ingredient.builder()
                                    .name(ingDto.getName())
                                    .build();
                            return ingredientRepository.save(newIngredient);
                        });

                // 2. 레시피-재료 연결
                RecipeIngredient ri = RecipeIngredient.builder()
                        .id(RecipeIngredientPK.builder()
                                .recipeId(recipe.getRecipeId())
                                .ingredientId(ingredient.getIngredientId())
                                .build())
                        .recipe(recipe)
                        .ingredient(ingredient)
                        .count(ingDto.getCount()) // 사용자가 보낸 count 저장
                        .build();

                recipeIngredientRepository.save(ri);
            }
        }


        return RecipeUpdateResponse.from(recipe)
                .toBuilder()
                .status("success")
                .build();
    }

    // =============== update =================
    @Transactional
    public RecipeUpdateResponse updateRecipe(String recipeId, RecipePostRequest dto) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not Found"));

        // 기존 Step 및 이미지 정보 가져오기
        List<RecipeSteps> existingSteps = recipeStepsRepository.findByRecipe_RecipeIdOrderByStepOrderAsc(recipeId);
        List<RecipeIngredient> existingIngredients = recipeIngredientRepository.findByRecipe_RecipeId(recipeId);

        // 기존 썸네일 값 따로 저장
        String oldThumbnail = recipe.getThumbnailImageUrl();

        // DB 업데이트
        recipe.setTitle(dto.getTitle());
        recipe.setThumbnailImageUrl(dto.getThumbnailImageUrl());
        recipe.setPeopleCount(dto.getPeopleCount());
        recipe.setPrepTime(dto.getPrepTime());
        recipe.setCookTime(dto.getCookTime());
        // kcal 업데이트
        if (dto.getKcal() != null) {
            recipe.setKcal(dto.getKcal());
        }
        recipeRepository.save(recipe);

        // 기존 Step 삭제 후 새 Step 저장
        recipeStepsRepository.deleteAll(existingSteps);

        List<RecipeSteps> newSteps = dto.getSteps().stream()
                .map(s -> RecipeSteps.builder()
                        .stepOrder(s.getStepOrder())
                        .contents(s.getContents() != null ? s.getContents() : "")
                        .imageUrl(s.getImageUrl() )
                        .recipe(recipe)
                        .build())
                .collect(Collectors.toList());
        recipeStepsRepository.saveAll(newSteps);

        // 🔹 Ingredient 업데이트
        // 기존 재료와 비교 후 추가/삭제/수정 처리
        List<RecipePostRequest.IngredientRequest> dtoIngredients = dto.getIngredients() != null
                ? dto.getIngredients()
                : new ArrayList<>();

        // 삭제: DB에는 있지만 DTO에는 없는 재료
        List<String> namesToDelete = existingIngredients.stream()
                .filter(ri -> dtoIngredients.stream()
                        .noneMatch(di -> di.getName().equals(ri.getIngredient().getName())))
                .map(ri -> ri.getIngredient().getName())
                .collect(Collectors.toList());

        for (String name : namesToDelete) {
            // 부분 삭제
            recipeIngredientRepository.deleteByRecipeIdAndIngredientName(recipeId, name);
        }

        // DB에 있거나 새로 추가할 재료 처리
        for (RecipePostRequest.IngredientRequest ingDto : dtoIngredients) {
            // 이름으로 Ingredient 조회, 없으면 새로 저장
            Ingredient ingredient = ingredientRepository.findByName(ingDto.getName())
                    .orElseGet(() -> ingredientRepository.save(
                            Ingredient.builder().name(ingDto.getName()).build()
                    ));

            // DB에 이미 존재하면 기존 객체 가져오기
            RecipeIngredient ri = existingIngredients.stream()
                    .filter(e -> e.getIngredient().getIngredientId().equals(ingredient.getIngredientId()))
                    .findFirst()
                    .orElseGet(() -> RecipeIngredient.builder()
                            .id(RecipeIngredientPK.builder()
                                    .recipeId(recipe.getRecipeId())
                                    .ingredientId(ingredient.getIngredientId())
                                    .build())
                            .recipe(recipe)
                            .ingredient(ingredient)
                            .build()
                    );

            // 수량 업데이트
            ri.setCount(ingDto.getCount());
            recipeIngredientRepository.save(ri);
        }

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


    /**
     * 냉장고 재료 기반 레시피 추천
     * @param ingredientNames
     * @param userId
     * @return List<RecipeRecommendationResponseDTO>
     */
    public List<RecipeRecommendationResponseDTO> recommendWithMatchCount(List<String> ingredientNames, String userId) {
        // 유저 검증
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("유저를 찾을 수 없습니다.");
        }

        List<RecipeMatchDTO> queryResult = recipeIngredientRepository.findRecipesWithMatchCount(ingredientNames);

        return queryResult
            .stream()
            .map(recipeMatchDTO -> {
                Recipe recipe = recipeMatchDTO.getRecipe();
                Long matchCount = recipeMatchDTO.getMatchCount();
                boolean isScrapped = scrapService.isScrapped(userId, recipe.getRecipeId());

                String thumbnailKey = recipe.getThumbnailImageUrl();
                String thumbnailUrl = null;
                if (thumbnailKey != null && !thumbnailKey.isEmpty()) {
                    thumbnailUrl = s3Service.generateDownloadPresignedUrls(List.of(thumbnailKey))
                                            .get(0)
                                            .get("downloadUrl");
                }

                // 서명된 url로 교체하여 dto 변환
                Recipe updateRecipe = recipe.toBuilder()
                                            .thumbnailImageUrl(thumbnailUrl)
                                            .build();
                RecipeListResponseDTO recipeListResponseDTO = RecipeListResponseDTO.from(updateRecipe);
                return RecipeRecommendationResponseDTO.of(recipeListResponseDTO, matchCount, isScrapped);
            })
            .toList();
    }

    // =============== Detail =================
    @Transactional(readOnly = true) // 조회만 하므로 readOnly
    public RecipeDetailResponse getRecipeDetail(String recipeId,
                                                CustomUserDetail userDetails) {

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not Found"));

        // 조회수는 DB에서 직접 가져오기
        int currentViews = recipeRepository.findViewsById(recipeId);

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
                .sorted(Comparator.comparingInt(StepResponse::getStepOrder))
                .collect(Collectors.toList());

        // Ingredient 목록 조회
        List<RecipeIngredient> recipeIngredients = recipeIngredientRepository.findByRecipe_RecipeId(recipeId);
        List<IngredientRecipeResponse> ingredientResponses = recipeIngredients.stream()
                .map(ri -> IngredientRecipeResponse.builder()
                        .name(ri.getIngredient().getName())
                        .count(ri.getCount())
                        .build())
                .collect(Collectors.toList());

        // 작성자 닉네임
        String authorNickname = recipe.getUser() != null ? recipe.getUser().getNickname() : "unknown";

        // 로그인 사용자 ID
        String currentUserId = userDetails != null ? userDetails.getUserId() : null;

        // 🔹 로그인한 사용자가 좋아요 눌렀는지 확인
        boolean liked = false;
        if (currentUserId != null) {
            liked = recipeLikeRepository.findByRecipe_RecipeIdAndUser_UserId(recipeId, currentUserId).isPresent();
        }

        return RecipeDetailResponse.from(
                        recipe.toBuilder().thumbnailImageUrl(thumbnailUrl).build(),
                        ingredientResponses,
                        stepResponses,
                        authorNickname,
                        currentUserId // isOwner 비교용
                ).toBuilder()
                .liked(liked) // 여기 추가
                .views(currentViews)
                .build();
    }

    /**
     * 조회수 증가: viewer(로그인 userId 또는 sessionId) 기반으로 캐시 체크 후 DB 증가
     * controller에서 viewer를 결정(로그인 우선, 아니면 session cookie 발급)해서 넘겨줄 것
     */
    @Transactional
    public RecipeViewsResponseDTO getRecipeWithViews(String recipeId, CustomUserDetail userDetails, String viewer) {

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not Found"));

        String ownerId = recipe.getUser() != null ? recipe.getUser().getUserId() : null;
        String loginUserId = (userDetails != null) ? userDetails.getUserId() : null;

        // 작성자 본인 접근이면 증가하지 않음
        if (loginUserId != null && Objects.equals(ownerId, loginUserId)) {
            int current = recipeRepository.findViewsById(recipeId);
            return RecipeViewsResponseDTO.of(current);
        }

        // viewer가 null이면(이상적으로는 controller에서 sessionId를 발급) 기존 방식 유지
        if (viewer == null) {
            recipeRepository.incrementViewsById(recipeId);
            int updated = recipeRepository.findViewsById(recipeId);
            return RecipeViewsResponseDTO.of(updated);
        }

        String key = recipeId + ":" + viewer;
        // putIfAbsent 원자적 삽입: 이미 있으면 null이 아님 -> 중복으로 간주
        Boolean prev = viewCache.asMap().putIfAbsent(key, Boolean.TRUE);
        if (prev != null) {
            int cur = recipeRepository.findViewsById(recipeId);
            return RecipeViewsResponseDTO.of(cur);
        }

        try {
            // 최초 뷰라면 DB 증가
            recipeRepository.incrementViewsById(recipeId);
            int updated = recipeRepository.findViewsById(recipeId);
            return RecipeViewsResponseDTO.of(updated);
        } catch (RuntimeException ex) {
            // DB 실패 시 캐시 롤백
            viewCache.invalidate(key);
            throw ex;
        }
    }


    // =============== delete =================
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
            log.error("S3 삭제 실패로 레시피 삭제 중단: {}", recipeId, e);
            throw new RuntimeException("S3 삭제 실패로 레시피 삭제 중단: " + recipeId, e);
        }

        // 4. DB 삭제 (S3 삭제 성공 시만 진행)
        recipeStepsRepository.deleteAll(steps); // CascadeType.REMOVE 사용 중이면 이 부분 생략 가능
        recipeRepository.delete(recipe);

        return true;
    }


}
