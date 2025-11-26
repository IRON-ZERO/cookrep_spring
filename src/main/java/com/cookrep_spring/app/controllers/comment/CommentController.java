package com.cookrep_spring.app.controllers.comment;

import com.cookrep_spring.app.dto.comment.request.CommentRequestDTO;
import com.cookrep_spring.app.dto.comment.response.CommentResponseDTO;
import com.cookrep_spring.app.security.CustomUserDetail;
import com.cookrep_spring.app.services.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ================== 댓글 생성 ==================
    @PostMapping
    public CommentResponseDTO createComment(
            @RequestBody CommentRequestDTO dto){
        return commentService.createComment(dto);
    }

    // ================== 댓글 수정 ==================
    @PutMapping("/{commentId}")
    public CommentResponseDTO updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetail userDetails) // 로그인한 사용자 정보
    {
        return commentService.updateComment(commentId, dto, userDetails.getUserId());
    }


    // ================== 댓글 삭제 ==================
    @DeleteMapping("/{commentId}")
    public CommentResponseDTO deleteComment(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @PathVariable Long commentId
            )
    {
        return commentService.deleteComment(commentId, userDetails.getUserId());
    }

    // ================== 레시피별 댓글 조회 ==================
    @GetMapping("/recipe/{recipeId}")
    public List<CommentResponseDTO> getCommentByRecipe(
            @PathVariable String recipeId,
            @AuthenticationPrincipal CustomUserDetail userDetails) { // 로그인 사용자
        String loginUserId = userDetails != null ? userDetails.getUserId() : null;
        return commentService.getCommentByRecipe(recipeId, loginUserId);
    }


}