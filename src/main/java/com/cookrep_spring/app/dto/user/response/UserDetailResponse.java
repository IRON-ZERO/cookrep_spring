package com.cookrep_spring.app.dto.user.response;


import com.cookrep_spring.app.models.user.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailResponse {
    private String email;
    private String nickname;
    private String firstName;
    private String lastName;
    private String country;
    private String city;

    public static UserDetailResponse from(User user) {
        return UserDetailResponse
            .builder()
            .email(user.getEmail())
            .nickname(user.getNickname())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .country(user.getCountry())
            .city(user.getCity())
            .build();
    }
}
