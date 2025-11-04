package com.cookrep_spring.app.models.recipe;

import com.cookrep_spring.app.models.ingredient.RecipeIngredient;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.cookrep_spring.app.models.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipe")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Recipe {

    @Id
    @Column(length = 50)
    private String recipeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;     // 작성자 ID (User FK)

    @Column(length = 100, nullable = false)
    private String title;       // 레시피 제목

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime  createdAt;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(length = 500)
    private String thumbnailImageUrl; // 썸네일 S3 URL

    @Column(columnDefinition = "INT DEFAULT 0")
    private int views;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int peopleCount;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int prepTime;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int cookTime;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int likesCount;

    // null 허용
    private Integer kcal;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> recipeIngredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeSteps> steps = new ArrayList<>();

}
