package com.cookrep_spring.app.models.recipe;

import com.cookrep_spring.app.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

// 하나의 유저가 한 레시피에 1번만 좋아요 — 중복 방지 가능 (unique 제약)
// 유저가 좋아요 누른 레시피 목록 조회 가능
// 특정 레시피 좋아요 수 계산 가능 (COUNT())
// 좋아요 취소 기능도 간단 (DELETE)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"recipe_id", "user_id"}))
public class RecipeLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
