USE cookrep;
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 테이블
CREATE TABLE user (
                      user_id VARCHAR(36) PRIMARY KEY,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      nickname VARCHAR(20) NOT NULL UNIQUE,
                      first_name VARCHAR(20) NOT NULL,
                      last_name VARCHAR(20) NOT NULL,
                      country VARCHAR(50),
                      city VARCHAR(50),
                      email VARCHAR(100) NOT NULL UNIQUE,
                      password VARCHAR(90)
);

-- 재료 테이블
CREATE TABLE ingredient (
                            ingredient_id INT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(50) NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 사용자-재료 관계 (사용자 냉장고)
CREATE TABLE useringredient (
                                user_id VARCHAR(36) NOT NULL,
                                ingredient_id INT NOT NULL,
                                PRIMARY KEY (user_id, ingredient_id),
                                FOREIGN KEY (user_id) REFERENCES user(user_id),
                                FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
);

-- 레시피 테이블
CREATE TABLE recipe (
                        recipe_id VARCHAR(50) PRIMARY KEY,
                        user_id VARCHAR(36),
                        title VARCHAR(100),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        thumbnail_image_url VARCHAR(500),
                        views INT DEFAULT 0,
                        people_count INT DEFAULT 0,
                        prep_time INT DEFAULT 0,
                        cook_time INT DEFAULT 0,
                        likes_count INT DEFAULT 0,
                        kcal INT,
                        FOREIGN KEY (user_id) REFERENCES user(user_id)
);

-- 레시피 단계 테이블
CREATE TABLE recipesteps (
                             step_id INT AUTO_INCREMENT PRIMARY KEY,
                             recipe_id VARCHAR(50) NOT NULL,
                             step_order INT NOT NULL,
                             contents TEXT,
                             image_url VARCHAR(500),
                             FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id)
);

-- 레시피-재료 테이블 (사용 재료)
CREATE TABLE recipeingredient (
                                  recipe_id VARCHAR(50),
                                  ingredient_id INT,
                                  count VARCHAR(20),
                                  PRIMARY KEY (recipe_id, ingredient_id),
                                  FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id),
                                  FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
);

-- 댓글 테이블
CREATE TABLE comment (
                         comment_id INT AUTO_INCREMENT PRIMARY KEY,
                         user_id VARCHAR(36) NOT NULL,
                         recipe_id VARCHAR(50) NOT NULL,
                         contents TEXT NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (user_id) REFERENCES user(user_id),
                         FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id)
);

-- 스크랩 테이블
CREATE TABLE scrap (
                       recipe_id VARCHAR(50) NOT NULL,
                       user_id VARCHAR(36) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       PRIMARY KEY (recipe_id, user_id),
                       FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id),
                       FOREIGN KEY (user_id) REFERENCES user(user_id)
);
