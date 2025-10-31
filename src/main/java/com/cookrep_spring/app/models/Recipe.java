package com.cookrep_spring.app.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.cookrep_spring.app.models.user.User;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Recipe {
    @Id
    @Column(name = "recipe_id",
            length = 50,
            nullable = false)
    private String recipeId; // 예: 20251001_001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        foreignKey = @ForeignKey(name = "fk_recipe_user")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user; // FK(User.user_id)

    @Column(length = 100)
    private String title;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "thumbnail_image_url", length = 500)
    private String thumbnailImageUrl;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int views;

    @Column(name = "people_count", columnDefinition = "INT DEFAULT 0")
    private int peopleCount;

    @Column(name = "prep_time", columnDefinition = "INT DEFAULT 0")
    private int prepTime;

    @Column(name = "cook_time", columnDefinition = "INT DEFAULT 0")
    private int cookTime;

    @Column(name = "`like`", columnDefinition = "INT DEFAULT 0")
    private int likesCount; // `like` 예약어 방지

    @Column
    private Integer kcal;
}