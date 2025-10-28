package com.cookrep_spring.app.controllers.user;

import com.cookrep_spring.app.dto.user.request.UserUpdateRequest;
import com.cookrep_spring.app.dto.user.response.UserDetailResponse;
import com.cookrep_spring.app.dto.user.response.UserUpdateResponse;
import com.cookrep_spring.app.models.user.User;
import com.cookrep_spring.app.services.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable String userId) {
        return userService.getUserDetail(userId)
                          .map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserUpdateResponse> updateUser(@PathVariable String userId,
                                                         @RequestBody UserUpdateRequest userUpdateRequest) {
        userUpdateRequest.setUserId(userId);
        return userService.update(userUpdateRequest)
                          .map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        // userId에 해당하는 User가 있을 경우 삭제 후 true. 없다면 false 반환
        boolean deleted = userService.deleteById(userId);

        if (deleted){
            return ResponseEntity.noContent().build();
        }else {
            return ResponseEntity.notFound().build();
        }
    }
}
