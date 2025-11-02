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
import com.cookrep_spring.app.services.ingredient.IngredientService;
import com.cookrep_spring.app.services.ingredient.UserIngredientService;
import com.cookrep_spring.app.services.scrap.ScrapService;
import com.cookrep_spring.app.services.user.UserService;
import lombok.RequiredArgsConstructor;
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
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable String userId) {
        UserDetailResponse response = userService.getUserDetail(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 유저 정보 수정
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<UserUpdateResponse> updateUser(@PathVariable String userId,
                                                         @RequestBody UserUpdateRequest userUpdateRequest) {
        userUpdateRequest.setUserId(userId);
        UserUpdateResponse response = userService.update(userUpdateRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 유저 삭제
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 유저가 가진 재료 조회
     */
    @GetMapping("/{userId}/ingredients")
    public ResponseEntity<List<UserIngredientResponseDTO>> getUserIngredients(@PathVariable String userId) {
        List<UserIngredientResponseDTO> ingredients = userIngredientService.findAllByUserId(userId);
        return ResponseEntity.ok(ingredients);
    }

    /**
     * 유저가 가진 재료 추가
     * - 반환 빈 값 "" 가능. (JS ES6에서 ""는 falsy)
     */
    @PostMapping("/{userId}/ingredients")
    public ResponseEntity<List<UserIngredientAddResponseDTO>> addUserIngredients(@PathVariable String userId,
                                                                                            @RequestBody UserIngredientAddRequestDTO ingredientAddRequestDTO) {
        String[] ingredientNames = ingredientAddRequestDTO.getIngredientNames();
        List<UserIngredientAddResponseDTO> result = userIngredientService.addIngredients(userId, ingredientNames);
        return ResponseEntity.ok(result);
    }

    /**
     * 유저가 가진 재료 삭제
     */
    @DeleteMapping("/{userId}/ingredients/{ingredientId}")
    public ResponseEntity<Void> deleteUserIngredients(@PathVariable String userId,
                                                      @PathVariable int ingredientId) {
        userIngredientService.deleteByUserIdAndIngredientId(userId, ingredientId);
        return ResponseEntity.noContent().build();
    }
    /**
     * 유저가 작성한 레시피 조회 (작성일 기준)
     */
    @GetMapping("/{userId}/recipes")
    public ResponseEntity<List<RecipeListResponseDTO>> getUserRecipes(@PathVariable String userId) {
        List<RecipeListResponseDTO> recipes = userService.getUserRecipes(userId);
        return ResponseEntity.ok(recipes);
    }

    /**
     * 유저가 스크랩한 레시피 조회
     */
    @GetMapping("/{userId}/scraps")
    public ResponseEntity<List<RecipeListResponseDTO>> getUserScraps(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getScrappedRecipes(userId));
    }

    /**
     * 해당 레시피 스크랩 등록
     */
    @PostMapping("/{userId}/scraps")
    public ResponseEntity<Void> addUserScraps(@PathVariable String userId,
                                               @RequestBody ScrapAddRequestDTO scrapAddRequestDTO){
        String recipeId = scrapAddRequestDTO.getRecipeId();
        System.out.println(recipeId);
        scrapService.scrapRecipe(userId, recipeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 해당 레시피 스크랩 취소
     */
    @DeleteMapping("/{userId}/scraps/{recipeId}")
    public ResponseEntity<Void> cancleUserScraps(@PathVariable String userId,
                                                 @PathVariable String recipeId){
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
