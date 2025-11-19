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
import org.w3c.dom.stylesheets.LinkStyle;

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
    public RecipeLikeResponseDTO toggleLike(RecipeLikeRequestDTO dto){
        String recipeId = dto.getRecipeId();
        String userId = dto.getUserId();

        // Recipe 와 User 엔티티 조회
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(()-> new RuntimeException("레시피를 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("사용자를 찾을 수 없습니다."));

        // 좋아요 이미 눌렀는지 확인
        // 좋아요가 존재하면 그 객체를 바로 삭제하거나 다른 정보를 활용 가능
        Optional<RecipeLike> existingLike = recipeLikeRepository.findByRecipe_RecipeIdAndUser_UserId(recipeId, userId);

        // 이미 좋아요 누른 상태면,
        if (existingLike.isPresent()){
           // 좋아요 삭제 (취소)
            recipeLikeRepository.delete(existingLike.get());

            // recipe 엔티티 likeCount 감소
            recipe.setLikesCount(Math.max(0, recipe.getLikesCount() -1 )); // 음수 방지
            recipeRepository.save(recipe);

            return RecipeLikeResponseDTO.builder()
                    .status("success")
                    .message("좋아요가 취소되었습니다.")
                    .recipeId(recipeId)
                    .userId(userId)
                    .likeCount(recipe.getLikesCount())
                    .build();
        } else {
            // 좋아요 추가
            RecipeLike recipeLike = new RecipeLike();
            recipeLike.setRecipe(recipe);
            recipeLike.setUser(user);
            recipeLikeRepository.save(recipeLike);

            // recipe 엔티티 likesCount 증가
            recipe.setLikesCount(recipe.getLikesCount() + 1);
            recipeRepository.save(recipe);

            return RecipeLikeResponseDTO.builder()
                    .status("success")
                    .message("좋아요가 등록되었습니다.")
                    .recipeId(recipeId)
                    .userId(userId)
                    .likeCount(recipe.getLikesCount())
                    .build();
        }
    }

    // =============== 특정 레시피 좋아요 누른 사용자 전체 조회 =================
    @Transactional
    public List<RecipeLikeUserResponseDTO> getUsersWhoLikedRecipe(String recipeId) {
        // Recipe 엔티티 조회
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("레시피를 찾을 수 없습니다."));

        // RecipeLike 엔티티 리스트 조회 후 User → DTO 변환
        List<RecipeLikeUserResponseDTO> users = recipeLikeRepository.findByRecipe_RecipeId(recipeId)
                .stream()
                .map(RecipeLike::getUser) // RecipeLike에서 User 추출
                .map(user -> RecipeLikeUserResponseDTO.builder()
                        .userId(user.getUserId())
                        .nickname(user.getNickname()) // User 엔티티 필드
                        .build())
                .toList();

        return users;
    }

}
