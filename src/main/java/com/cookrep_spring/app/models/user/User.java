package com.cookrep_spring.app.models.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    private String userId;
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
    private String nickname;
    private String firstName;
    private String lastName;
    private String country;
    private String city;
}

