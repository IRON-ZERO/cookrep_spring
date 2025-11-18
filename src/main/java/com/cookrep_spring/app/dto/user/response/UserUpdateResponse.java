package com.cookrep_spring.app.dto.user.response;

import com.cookrep_spring.app.models.user.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateResponse {
    private String firstName;
    private String lastName;
    private String country;
    private String city;

    public static UserUpdateResponse from(User user){
        return UserUpdateResponse
            .builder()
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .country(user.getCountry())
            .city(user.getCity())
            .build();
    }
}