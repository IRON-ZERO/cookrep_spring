package com.cookrep_spring.app.controllers.user;

import com.cookrep_spring.app.dto.recipe.response.RecipeResponseDTO;
import com.cookrep_spring.app.dto.user.request.UserUpdateRequest;
import com.cookrep_spring.app.dto.user.response.UserDetailResponse;
import com.cookrep_spring.app.dto.user.response.UserUpdateResponse;
import com.cookrep_spring.app.models.ingredient.Ingredient;
import com.cookrep_spring.app.services.ingredient.IngredientService;
import com.cookrep_spring.app.services.ingredient.UserIngredientService;
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

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable String userId) {
        return userService.getUserDetail(userId)
                          .map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserUpdateResponse> updateUser(@PathVariable String userId,
                                                         @RequestBody UserUpdateRequest userUpdateRequest) {
        userUpdateRequest.setUserId(userId);
        return userService.update(userUpdateRequest)
                          .map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        // userId에 해당하는 User가 있을 경우 삭제 후 true. 없다면 false 반환
        boolean deleted = userService.deleteById(userId);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }else {
            return ResponseEntity.notFound().build();
        }
    }

    // 유저가 가진 재료 조회
    @GetMapping("/{userId}/ingredients")
    public ResponseEntity<List<Ingredient>> getUserIngredients(@PathVariable String userId) {
        return userIngredientService.findAllByUserId(userId)
                                    .map(ResponseEntity::ok)
                                    .orElse(ResponseEntity.notFound().build());
    }

    // 유저가 가진 재료 추가
    // 반환 빈 값 "" 가능. es6에서 ""는 falsy
    @PostMapping("/{userId}/ingredients")
    public ResponseEntity<List<Ingredient>> addUserIngredients(@PathVariable String userId,
                                                               @RequestBody String[] ingredientNames) {
        return ResponseEntity.ok(userIngredientService.addIngredients(userId,ingredientNames));
    }

    // 유저가 가진 재료 삭제
    @DeleteMapping("/{userId}/ingredients")
    public ResponseEntity<Void> deleteUserIngredients(@PathVariable String userId,
                                                      @RequestBody int ingredientId) {
        boolean deleted = userIngredientService.deleteByUserIdAndIngredientId(userId, ingredientId);
        return deleted ? ResponseEntity.noContent().build()
                       : ResponseEntity.notFound().build();
    }

    // 유저가 검색하고 싶은 재료 Id들로 레시피 조회
    // 반환 빈 값 "" 가능. es6에서 ""는 falsy
    @PostMapping("/search-recipes")
    public  ResponseEntity<Map<RecipeResponseDTO, Integer>> findRecipesByIngredientIds(@RequestBody List<Integer> ingredientIds) {
        List<String> ingredientNames = ingredientService.findNamesByIds(ingredientIds);
        return ResponseEntity.ok(userIngredientService.recommendWithMatchCount(ingredientNames));
    }

}
