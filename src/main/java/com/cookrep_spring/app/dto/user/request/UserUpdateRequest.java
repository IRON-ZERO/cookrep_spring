package com.cookrep_spring.app.dto.user.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String userId;
    private String firstName;
    private String lastName;
    private String country;
    private String city;
}
