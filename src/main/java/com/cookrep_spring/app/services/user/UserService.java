package com.cookrep_spring.app.services.user;

import com.cookrep_spring.app.dto.recipe.response.RecipeListResponseDTO;
import com.cookrep_spring.app.dto.user.request.UserUpdateRequest;
import com.cookrep_spring.app.dto.user.response.UserDetailResponse;
import com.cookrep_spring.app.dto.user.response.UserUpdateResponse;
import com.cookrep_spring.app.models.Recipe;
import com.cookrep_spring.app.repositories.recipe.RecipeRepository;
import com.cookrep_spring.app.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    public Optional<UserDetailResponse> getUserDetail(String id) {
        return userRepository.findById(id).map(UserDetailResponse::from);
    }

    @Transactional
    public Optional<UserUpdateResponse> update(UserUpdateRequest userInput) {
        return userRepository
            .findById(userInput.getUserId())
            .map(user -> {

                // null 값이 아닌 것들만 업데이트
                if(userInput.getFirstName() != null) user.setFirstName(userInput.getFirstName());
                if(userInput.getLastName() != null) user.setLastName(userInput.getLastName());
                if(userInput.getCountry() != null) user.setCountry(userInput.getCountry());
                if(userInput.getCity() != null) user.setCity(userInput.getCity());

                return UserUpdateResponse.from(user);
            });
    }

    @Transactional
    public boolean deleteById(String userId) {
        return userRepository.findById(userId)
                             .map(user -> {
                                 userRepository.delete(user);
                                 return true;
                             })
                             .orElse(false);
    }

    public List<RecipeListResponseDTO> getUserRecipes(String userId) {
        return recipeRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
            .stream().map(RecipeListResponseDTO::from).toList();
    }

}
