package com.cookrep_spring.app.models.scrap;

import com.cookrep_spring.app.models.recipe.Recipe;
import com.cookrep_spring.app.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scrap{

    @EmbeddedId
    private ScrapPK id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id",
                nullable = false,
                foreignKey = @ForeignKey(name = "fk_scrap_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne
    @MapsId("recipeId")
    @JoinColumn(name = "recipe_id",
                nullable = false,
                foreignKey = @ForeignKey(name = "fk_scrap_recipe"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Recipe recipe;

    @Column(name = "created_at",
            insertable = false,
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}