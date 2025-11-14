package com.cookrep_spring.app.controllers.recipe;

import com.cookrep_spring.app.dto.recipe.response.RecipeListResponse;
import com.cookrep_spring.app.services.recipe.RecipeService;
import com.cookrep_spring.app.utils.S3Service;
import com.cookrep_spring.app.dto.recipe.request.RecipePostRequest;
import com.cookrep_spring.app.dto.recipe.response.RecipeDetailResponse;
import com.cookrep_spring.app.dto.recipe.response.RecipeUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:5173")
public class RecipeController {
    private final RecipeService recipeService;
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
    public ResponseEntity<RecipeUpdateResponse> registerRecipe(
            @PathVariable String userId,
            @RequestBody RecipePostRequest request)
    {
        RecipeUpdateResponse response = recipeService.saveRecipe(userId, request);
        return ResponseEntity.ok(response);
    }

    //================== update =================
    @PutMapping("/{recipeId}")
    public ResponseEntity<RecipeUpdateResponse> updateRecipe(
            @PathVariable String recipeId,
            @RequestBody RecipePostRequest request)
    {
//        request.setRecipeId(recipeId);
        RecipeUpdateResponse response = recipeService.updateRecipe(recipeId, request);
        return ResponseEntity.ok(response);
    }

    //================== List =================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecipeListResponse>> getRecipeList(@PathVariable String userId){
        List<RecipeListResponse> response = recipeService.getRecipeList(userId);
        return ResponseEntity.ok(response);
    }

    //================== detail =================
    @GetMapping("/{recipeId}")
    public ResponseEntity<RecipeDetailResponse> getRecipeDetail(@PathVariable String recipeId){
        RecipeDetailResponse response = recipeService.getRecipeDetail(recipeId);
        return ResponseEntity.ok(response);

    }


    //================== delete =================
    @DeleteMapping("/{recipeId}")
    public ResponseEntity<?> deleteRecipe(@PathVariable String recipeId) {
        try {
            recipeService.deleteRecipe(recipeId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
