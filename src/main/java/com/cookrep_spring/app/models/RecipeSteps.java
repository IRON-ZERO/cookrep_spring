package com.cookrep_spring.app.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recipesteps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeSteps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stepId;

    private int stepOrder;
    private String contents;
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)  // 반드시 명시해야 함
    private Recipe recipe;
}
