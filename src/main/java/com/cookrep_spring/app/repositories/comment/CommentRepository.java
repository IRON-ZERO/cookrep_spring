package com.cookrep_spring.app.repositories.comment;

import com.cookrep_spring.app.models.comment.Comment;
import com.cookrep_spring.app.models.recipe.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByRecipeOrderByCreatedAtDesc(Recipe recipe);
}
