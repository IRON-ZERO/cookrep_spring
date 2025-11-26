package com.cookrep_spring.app.dto.comment.response;

import com.cookrep_spring.app.models.comment.Comment;
import com.cookrep_spring.app.models.recipe.Recipe;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class CommentResponseDTO {
    private String status;        // 성공/실패 여부
    private String message;       // 메시지
    private Long commentId;       // 댓글 ID
    private String recipeId;      // 레시피 ID
    private String userId;        // 작성자 ID
    private String nickname;      // 작성자 닉네임
    private String contents;      // 댓글 내용
    private LocalDateTime createdAt;  // 작성 시간
    private LocalDateTime updatedAt;  // 수정 시간
    private boolean owner; // 로그인 사용자 기준 작성자인지 여부

    public static CommentResponseDTO from(
            Comment comment, String status, String message, String currentUserId, Recipe recipe) {
        return CommentResponseDTO.builder()
                .status(status)
                .message(message)
                .commentId(comment.getCommentId())
                .recipeId(comment.getRecipe().getRecipeId())
                .userId(comment.getUser().getUserId())
                .nickname(comment.getUser().getNickname()) // nickname 추가
                .contents(comment.getContents())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .owner(currentUserId != null && comment.getUser().getUserId().equals(currentUserId))
                .build();
    }
}

