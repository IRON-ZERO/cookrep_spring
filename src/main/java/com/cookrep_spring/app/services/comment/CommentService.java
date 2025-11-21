package com.cookrep_spring.app.services.comment;

import com.cookrep_spring.app.dto.comment.request.CommentRequestDTO;
import com.cookrep_spring.app.dto.comment.response.CommentResponseDTO;
import com.cookrep_spring.app.models.comment.Comment;
import com.cookrep_spring.app.models.recipe.Recipe;
import com.cookrep_spring.app.models.user.User;
import com.cookrep_spring.app.repositories.comment.CommentRepository;
import com.cookrep_spring.app.repositories.recipe.RecipeRepository;
import com.cookrep_spring.app.repositories.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    // =============== comment create =================
    @Transactional
    public CommentResponseDTO createComment(CommentRequestDTO dto){
        String recipeId = dto.getRecipeId();
        String userId = dto.getUserId();
        String contents = dto.getContents();


        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("레시피를 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));


        //댓글 생성
        Comment comment = Comment.builder()
                .recipe(recipe)
                .user(user)
                .contents(contents)
                .build();

        // DB 저장
        commentRepository.save(comment);

        // ResponseDTO 생성
        return CommentResponseDTO.from(
                comment,
                "success",
                "댓글이 등록되었습니다.",
                userId,
                recipe
        );

    }

    // =============== comment update =================
    @Transactional
    public CommentResponseDTO updateComment(Long commentId, CommentRequestDTO dto, String loginUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));


        // 작성자만 수정 가능
        if (!comment.getUser().getUserId().equals(loginUserId)) {
            throw new RuntimeException("작성자만 댓글을 수정할 수 있습니다.");
        }

        comment.setContents(dto.getContents());

        return CommentResponseDTO.from(
                comment,
                "success",
                "댓글이 수정되었습니다.",
                loginUserId,
                comment.getRecipe()
        );

    }


    // =============== comment delete =================
    @Transactional
    public CommentResponseDTO deleteComment(Long commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        // 작성자만 삭제 가능
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("작성자만 댓글을 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);

        return CommentResponseDTO.builder()
                .status("success")
                .message("댓글이 삭제되었습니다.")
                .commentId(commentId)
                .build();

    }

    // =============== comment list by recipe =================
    // 레시피별 댓글 조회 (로그인 사용자 기준 owner 포함)
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentByRecipe(String recipeId, String loginUserId){
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("레시피를 찾을 수 없습니다."));

        return commentRepository.findAllByRecipeOrderByCreatedAtDesc(recipe)
                .stream()
                .map(c -> CommentResponseDTO.from(c, "success", "댓글 조회 성공", loginUserId, recipe))
                .toList();

    }


}
