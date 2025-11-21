package com.cookrep_spring.app.dto.recipe.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RecipeSearchResultDto {
  private String recipeId;
  private String title;
  private String thumbnailImageUrl;
  private String cookLevel;
  private int views;
  private int peopleCount;
  private int prepTime;
  private int cookTime;
  private int likesCount;
  private int kcal;

  @Builder
  public RecipeSearchResultDto(String recipeId, String title, String thumbnailImageUrl, int views, int peopleCount, int prepTime, int cookTime, int likesCount, int kcal) {
    this.recipeId = recipeId;
    this.title = title;
    this.thumbnailImageUrl = thumbnailImageUrl;
    this.views = views;
    this.peopleCount = peopleCount;
    this.prepTime = prepTime;
    this.cookTime = cookTime;
    this.likesCount = likesCount;
    this.kcal = kcal;

    this.cookLevel = calculateCookLevel(prepTime, cookTime);
  }

  private String calculateCookLevel(int pTime, int cTime) {
    String easy = "EASY", normal = "NORMAL", hard = "HARD";
    boolean easyCoast = (pTime < 40 && cTime < 40);
    boolean hardCoast = (pTime > 50 && cTime > 60) || cTime > 100;
    if (easyCoast) {
      return easy;
    } else if (hardCoast) {
      return hard;
    }
    return normal;
  }
}
