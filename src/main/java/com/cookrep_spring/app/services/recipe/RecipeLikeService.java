package com.cookrep_spring.app.services.recipe;

import com.cookrep_spring.app.dto.recipe.request.RecipeLikeRequestDTO;
import com.cookrep_spring.app.dto.recipe.response.RecipeLikeResponseDTO;
import com.cookrep_spring.app.dto.recipe.response.RecipeLikeUserResponseDTO;
import com.cookrep_spring.app.models.recipe.Recipe;
import com.cookrep_spring.app.models.recipe.RecipeLike;
import com.cookrep_spring.app.models.user.User;
import com.cookrep_spring.app.repositories.recipe.RecipeLikeRepository;
import com.cookrep_spring.app.repositories.recipe.RecipeRepository;
import com.cookrep_spring.app.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeLikeService {

    private final RecipeLikeRepository recipeLikeRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    // =============== toggleLike (좋아요 추가/삭제) =================
    @Transactional
    public RecipeLikeResponseDTO toggleLike(RecipeLikeRequestDTO dto) {
        String recipeId = dto.getRecipeId();
        String userId = dto.getUserId();


        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("레시피를 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<RecipeLike> existingLike =
                recipeLikeRepository.findByRecipe_RecipeIdAndUser_UserId(recipeId, userId);

        // 이미 좋아요 누른 상태 → 삭제
        if (existingLike.isPresent()) {

            recipeLikeRepository.delete(existingLike.get());

            recipeRepository.decreaseLike(recipeId); // DB likesCount -1

            // ← UPDATE 후 다시 DB에서 정확한 likeCount 조회
            int updatedCount = recipeRepository.findLikesCountByRecipeId(recipeId);


            return RecipeLikeResponseDTO.builder()
                    .status("success")
                    .message("좋아요가 취소되었습니다.")
                    .recipeId(recipeId)
                    .userId(userId)
                    .likeCount(updatedCount)
                    .liked(false)
                    .build();
        }

        // 좋아요 추가
        RecipeLike recipeLike = new RecipeLike();
        recipeLike.setRecipe(recipe);
        recipeLike.setUser(user);

        recipeLikeRepository.save(recipeLike);

        recipeRepository.increaseLike(recipeId); // DB likesCount +1

        // ← UPDATE 후 다시 DB에서 정확한 likeCount 조회
        int updatedCount = recipeRepository.findLikesCountByRecipeId(recipeId);


        return RecipeLikeResponseDTO.builder()
                .status("success")
                .message("좋아요가 등록되었습니다.")
                .recipeId(recipeId)
                .userId(userId)
                .likeCount(updatedCount)
                .liked(true)
                .build();
    }


    // =============== 특정 레시피 좋아요 누른 사용자 전체 조회 =================
    @Transactional(readOnly = true)
    public List<RecipeLikeUserResponseDTO> getUsersWhoLikedRecipe(String recipeId) {

        recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("레시피를 찾을 수 없습니다."));

        return recipeLikeRepository.findByRecipe_RecipeId(recipeId)
                .stream()
                .map(RecipeLike::getUser)
                .map(user -> RecipeLikeUserResponseDTO.builder()
                        .userId(user.getUserId())
                        .nickname(user.getNickname())
                        .build())
                .toList();
    }
}
