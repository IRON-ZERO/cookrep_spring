package com.cookrep_spring.app.controllers.recipe;

import com.cookrep_spring.app.dto.recipe.request.RecipeLikeRequestDTO;
import com.cookrep_spring.app.dto.recipe.request.RecipeSearchByIngredientsRequestDTO;
import com.cookrep_spring.app.dto.recipe.response.*;
import com.cookrep_spring.app.security.CustomUserDetail;
import com.cookrep_spring.app.services.ingredient.IngredientService;
import com.cookrep_spring.app.services.recipe.RecipeLikeService;
import com.cookrep_spring.app.services.recipe.RecipeService;
import com.cookrep_spring.app.utils.S3Service;
import com.cookrep_spring.app.dto.recipe.request.RecipePostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;
    private final IngredientService ingredientService;

    private final S3Service s3Service;
    private final RecipeLikeService recipeLikeService;


    //================== upload =================
    //s3 서명된 url 생성 api
    @PostMapping("/presigned")
    public ResponseEntity<List<Map<String, String>>> getPresignedUrls(@RequestBody List<String> fileNames){
        if (fileNames == null || fileNames.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Map<String , String>> urls = s3Service.generatePresignedUrls(fileNames);
        return ResponseEntity.ok(urls);
    }


    // 클라이언트에서 s3 업로드 완료 후, db에 최종 업로드 api
    @PostMapping("/{userId}")
    @PreAuthorize("#userId == #userDetails.userId")
    public ResponseEntity<RecipeUpdateResponse> registerRecipe(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @RequestBody RecipePostRequest request)
    {
        RecipeUpdateResponse response = recipeService.saveRecipe(userDetails.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    //================== update =================
    @PutMapping("/{recipeId}")
    @PreAuthorize("@recipeSecurity.isOwner(#recipeId, #userDetails)")
    public ResponseEntity<RecipeUpdateResponse> updateRecipe(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @PathVariable String recipeId,
            @RequestBody RecipePostRequest request)
    {
        RecipeUpdateResponse response = recipeService.updateRecipe(recipeId, request);
        return ResponseEntity.ok(response);
    }

    //================== List =================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecipeListResponse>> getRecipeList(@AuthenticationPrincipal CustomUserDetail userDetails){
        List<RecipeListResponse> response = recipeService.getRecipeList(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    //================== detail =================
    @GetMapping("/{recipeId}")
    public ResponseEntity<RecipeDetailResponse> getRecipeDetail(
            @PathVariable String recipeId,
            @AuthenticationPrincipal CustomUserDetail userDetails)
    {
        RecipeDetailResponse response = recipeService.getRecipeDetail(recipeId, userDetails);
        return ResponseEntity.ok(response);

    }


    //================== delete =================
    @DeleteMapping("/{recipeId}")
    @PreAuthorize("@recipeSecurity.isOwner(#recipeId, #userDetails)")
    public ResponseEntity<?> deleteRecipe(@PathVariable String recipeId, @AuthenticationPrincipal CustomUserDetail userDetails) {
        try {
            recipeService.deleteRecipe(recipeId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    /**
     * 유저가 검색하고 싶은 재료 ID들로 레시피 조회
     * - 반환 빈 값 "" 가능. (JS ES6에서 ""는 falsy)
     */
    @PostMapping("/search")
    public ResponseEntity<List<RecipeRecommendationResponseDTO>> findRecipesByIngredientIds(
            @RequestBody RecipeSearchByIngredientsRequestDTO recipeSearchByIngredientsRequestDTO,
            @AuthenticationPrincipal CustomUserDetail userDetails) {
        List<Integer> ingredientIds = recipeSearchByIngredientsRequestDTO.getIngredientIds();
        List<String> ingredientNames = ingredientService.findNamesByIds(ingredientIds);
        String userId = userDetails.getUserId();
        List<RecipeRecommendationResponseDTO> result = recipeService.recommendWithMatchCount(ingredientNames,userId);
        return ResponseEntity.ok(result);
    }

    // =============== toggleLike (좋아요 추가/삭제) =================
    @PostMapping("/like/{recipeId}")
    public ResponseEntity<RecipeLikeResponseDTO> recipeLikeToggle(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @PathVariable String recipeId
    ) {
        RecipeLikeRequestDTO request = RecipeLikeRequestDTO.builder()
                .recipeId(recipeId)
                .userId(userDetails.getUserId())
                .build();

        RecipeLikeResponseDTO response = recipeLikeService.toggleLike(request);
        return ResponseEntity.ok(response);
    }


    // =============== 특정 레시피 좋아요 누른 사용자 전체 조회 =================
    @GetMapping("/like/{recipeId}")
    public ResponseEntity<List<RecipeLikeUserResponseDTO>> getUsersWhoLikedRecipe(
            @PathVariable String recipeId) {

        List<RecipeLikeUserResponseDTO> users = recipeLikeService.getUsersWhoLikedRecipe(recipeId);
        return ResponseEntity.ok(users);
    }
}
