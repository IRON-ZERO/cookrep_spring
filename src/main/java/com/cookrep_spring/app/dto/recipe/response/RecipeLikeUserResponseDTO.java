package com.cookrep_spring.app.dto.recipe.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class RecipeLikeUserResponseDTO {
    private String userId;
    private String nickname;
}
