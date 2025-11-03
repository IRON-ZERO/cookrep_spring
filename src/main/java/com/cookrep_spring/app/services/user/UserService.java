package com.cookrep_spring.app.services.user;

import com.cookrep_spring.app.dto.recipe.response.RecipeListResponseDTO;
import com.cookrep_spring.app.dto.user.request.UserUpdateRequest;
import com.cookrep_spring.app.dto.user.response.UserDetailResponse;
import com.cookrep_spring.app.dto.user.response.UserUpdateResponse;
import com.cookrep_spring.app.models.user.User;
import com.cookrep_spring.app.repositories.recipe.RecipeRepository;
import com.cookrep_spring.app.repositories.scrap.ScrapRepository;
import com.cookrep_spring.app.repositories.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final ScrapRepository scrapRepository;

    /**
     * 유저 상세 조회
     */
    public UserDetailResponse getUserDetail(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        return UserDetailResponse.from(user);
    }

    /**
     * 유저 정보 수정
     */
    @Transactional
    public UserUpdateResponse update(UserUpdateRequest userInput) {
        User user = userRepository.findById(userInput.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        if (userInput.getFirstName() != null) user.setFirstName(userInput.getFirstName());
        if (userInput.getLastName() != null) user.setLastName(userInput.getLastName());
        if (userInput.getCountry() != null) user.setCountry(userInput.getCountry());
        if (userInput.getCity() != null) user.setCity(userInput.getCity());

        return UserUpdateResponse.from(user);
    }

    /**
     * 유저 삭제
     */
    @Transactional
    public void deleteById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        userRepository.delete(user);
    }

    /**
     * 유저가 작성한 레시피 조회
     */
    public List<RecipeListResponseDTO> getUserRecipes(String userId) {
        // [1] 유저 조회 확인
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("유저를 찾을 수 없습니다.");
        }
        // [2] scraped recipes 목록 조회
        List<String> scrappedIds = scrapRepository.findRecipeIdsByUserId(userId);
        Set<String> scrappedSet = new HashSet<>(scrappedIds);

        // [3] 레시피와 스크랩 여부 반환
        return recipeRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(recipe -> RecipeListResponseDTO.of(
                        recipe,
                        scrappedSet.contains(recipe.getRecipeId())
                ))
                .toList();
    }

    /**
     * 유저가 스크랩한 레시피 조회
     */
    public List<RecipeListResponseDTO> getScrappedRecipes(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("유저를 찾을 수 없습니다.");
        }
        return scrapRepository.findRecipesByUserId(userId)
                .stream()
                .map(recipe->RecipeListResponseDTO.of(recipe, true))
                .toList();
    }
}