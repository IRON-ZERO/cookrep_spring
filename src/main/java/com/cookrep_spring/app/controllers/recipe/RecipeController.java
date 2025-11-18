package com.cookrep_spring.app.controllers.recipe;

import com.cookrep_spring.app.dto.recipe.request.RecipeSearchByIngredientsRequestDTO;
import com.cookrep_spring.app.dto.recipe.response.RecipeListResponse;
import com.cookrep_spring.app.dto.recipe.response.RecipeListResponseDTO;
import com.cookrep_spring.app.security.CustomUserDetail;
import com.cookrep_spring.app.services.ingredient.IngredientService;
import com.cookrep_spring.app.services.ingredient.UserIngredientService;
import com.cookrep_spring.app.services.recipe.RecipeService;
import com.cookrep_spring.app.utils.S3Service;
import com.cookrep_spring.app.dto.recipe.request.RecipePostRequest;
import com.cookrep_spring.app.dto.recipe.response.RecipeDetailResponse;
import com.cookrep_spring.app.dto.recipe.response.RecipeUpdateResponse;
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
    private final UserIngredientService userIngredientService;

    private final S3Service s3Service;

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
//        request.setRecipeId(recipeId);
        RecipeUpdateResponse response = recipeService.updateRecipe(recipeId, request);
        return ResponseEntity.ok(response);
    }

    //================== List =================
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == #userDetails.userId")
    public ResponseEntity<List<RecipeListResponse>> getRecipeList(@AuthenticationPrincipal CustomUserDetail userDetails){
        List<RecipeListResponse> response = recipeService.getRecipeList(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    //================== detail =================
    @GetMapping("/{recipeId}")
    @PreAuthorize("@recipeSecurity.isOwner(#recipeId, #userDetails)")
    public ResponseEntity<RecipeDetailResponse> getRecipeDetail(@PathVariable String recipeId, @AuthenticationPrincipal CustomUserDetail userDetails){
        RecipeDetailResponse response = recipeService.getRecipeDetail(recipeId);
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
    public ResponseEntity<Map<RecipeListResponseDTO, Integer>> findRecipesByIngredientIds(
            @RequestBody RecipeSearchByIngredientsRequestDTO recipeSearchByIngredientsRequestDTO) {
        List<Integer> ingredientIds = recipeSearchByIngredientsRequestDTO.getIngredientIds();
        List<String> ingredientNames = ingredientService.findNamesByIds(ingredientIds);
        Map<RecipeListResponseDTO, Integer> result = userIngredientService.recommendWithMatchCount(ingredientNames);
        return ResponseEntity.ok(result);
    }
}
