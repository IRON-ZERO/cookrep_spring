package com.cookrep_spring.app.controllers.user;

import com.cookrep_spring.app.dto.ingredient.request.UserIngredientAddRequestDTO;
import com.cookrep_spring.app.dto.ingredient.response.UserIngredientAddResponseDTO;
import com.cookrep_spring.app.dto.ingredient.response.UserIngredientResponseDTO;
import com.cookrep_spring.app.dto.recipe.request.RecipeSearchByIngredientsRequestDTO;
import com.cookrep_spring.app.dto.recipe.response.RecipeListResponseDTO;
import com.cookrep_spring.app.dto.scrap.request.ScrapAddRequestDTO;
import com.cookrep_spring.app.dto.user.request.UserUpdateRequest;
import com.cookrep_spring.app.dto.user.response.UserDetailResponse;
import com.cookrep_spring.app.dto.user.response.UserUpdateResponse;
import com.cookrep_spring.app.security.CustomUserDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.cookrep_spring.app.services.ingredient.IngredientService;
import com.cookrep_spring.app.services.ingredient.UserIngredientService;
import com.cookrep_spring.app.services.scrap.ScrapService;
import com.cookrep_spring.app.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserIngredientService userIngredientService;
    private final IngredientService ingredientService;
    private final ScrapService scrapService;

    /**
     * 유저 상세 조회
     */
    @GetMapping("/me")
    public ResponseEntity<UserDetailResponse> getUserDetail(@AuthenticationPrincipal CustomUserDetail userDetails) {
        String userId = userDetails.getUserId();
        UserDetailResponse response = userService.getUserDetail(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 유저 정보 수정
     */
    @PatchMapping("/me")
    public ResponseEntity<UserUpdateResponse> updateUser(@AuthenticationPrincipal CustomUserDetail userDetails,
                                                         @RequestBody UserUpdateRequest userUpdateRequest) {
        String userId = userDetails.getUserId();
        userUpdateRequest.setUserId(userId);
        UserUpdateResponse response = userService.update(userUpdateRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 유저 삭제
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal CustomUserDetail userDetails) {
        String userId = userDetails.getUserId();
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 유저가 가진 재료 조회
     */
    @GetMapping("/me/ingredients")
    public ResponseEntity<List<UserIngredientResponseDTO>> getUserIngredients(@AuthenticationPrincipal CustomUserDetail userDetails) {
        String userId = userDetails.getUserId();
        List<UserIngredientResponseDTO> ingredients = userIngredientService.findAllByUserId(userId);
        return ResponseEntity.ok(ingredients);
    }

    /**
     * 유저가 가진 재료 추가
     * - 반환 빈 값 "" 가능. (JS ES6에서 ""는 falsy)
     */
    @PostMapping("/me/ingredients")
    public ResponseEntity<List<UserIngredientAddResponseDTO>> addUserIngredients(@AuthenticationPrincipal CustomUserDetail userDetails,
                                                                                 @RequestBody UserIngredientAddRequestDTO ingredientAddRequestDTO) {
        String userId = userDetails.getUserId();
        String[] ingredientNames = ingredientAddRequestDTO.getIngredientNames();
        if (ingredientNames == null || ingredientNames.length == 0) {
            return ResponseEntity.badRequest().body(null);
        }
        if (ingredientNames == null || ingredientNames.length == 0) {
            return ResponseEntity.badRequest().body(null);
        }
        List<UserIngredientAddResponseDTO> result = userIngredientService.addIngredients(userId, ingredientNames);
        return ResponseEntity.ok(result);
    }

    /**
     * 유저가 가진 재료 삭제
     */
    @DeleteMapping("/me/ingredients/{ingredientId}")
    public ResponseEntity<Void> deleteUserIngredients(@AuthenticationPrincipal CustomUserDetail userDetails,
                                                      @PathVariable int ingredientId) {
        String userId = userDetails.getUserId();
        userIngredientService.deleteByUserIdAndIngredientId(userId, ingredientId);
        return ResponseEntity.noContent().build();
    }
    /**
     * 유저가 작성한 레시피 조회 (작성일 기준)
     */
    @GetMapping("/me/recipes")
    public ResponseEntity<List<RecipeListResponseDTO>> getUserRecipes(@AuthenticationPrincipal CustomUserDetail userDetails) {
        String userId = userDetails.getUserId();
        List<RecipeListResponseDTO> recipes = userService.getUserRecipes(userId);
        return ResponseEntity.ok(recipes);
    }

    /**
     * 유저가 스크랩한 레시피 조회
     */
    @GetMapping("/me/scraps")
    public ResponseEntity<List<RecipeListResponseDTO>> getUserScraps(@AuthenticationPrincipal CustomUserDetail userDetails) {
        String userId = userDetails.getUserId();
        return ResponseEntity.ok(userService.getScrappedRecipes(userId));
    }

    /**
     * 해당 레시피 스크랩 등록
     */
    @PostMapping("/me/scraps")
    public ResponseEntity<Void> addUserScraps(@AuthenticationPrincipal CustomUserDetail userDetails,
                                              @RequestBody ScrapAddRequestDTO scrapAddRequestDTO){
        String userId = userDetails.getUserId();
        String recipeId = scrapAddRequestDTO.getRecipeId();
        scrapService.scrapRecipe(userId, recipeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 해당 레시피 스크랩 취소
     */
    @DeleteMapping("/me/scraps/{recipeId}")
    public ResponseEntity<Void> cancelUserScraps(@AuthenticationPrincipal CustomUserDetail userDetails,
                                                 @PathVariable String recipeId){
        String userId = userDetails.getUserId();
        scrapService.cancelScrap(userId, recipeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 유저가 검색하고 싶은 재료 ID들로 레시피 조회
     * - 반환 빈 값 "" 가능. (JS ES6에서 ""는 falsy)
     */
    @PostMapping("/search-recipes")
    public ResponseEntity<Map<RecipeListResponseDTO, Integer>> findRecipesByIngredientIds(
        @RequestBody RecipeSearchByIngredientsRequestDTO recipeSearchByIngredientsRequestDTO) {
        List<Integer> ingredientIds = recipeSearchByIngredientsRequestDTO.getIngredientIds();
        List<String> ingredientNames = ingredientService.findNamesByIds(ingredientIds);
        Map<RecipeListResponseDTO, Integer> result = userIngredientService.recommendWithMatchCount(ingredientNames);
        return ResponseEntity.ok(result);
    }

}
