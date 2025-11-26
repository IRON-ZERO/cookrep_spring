package com.cookrep_spring.app.dto.recipe.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StepResponse {
    private int stepOrder;
    private String contents;
    private String imageUrl;
}
