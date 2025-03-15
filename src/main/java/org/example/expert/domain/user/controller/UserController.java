package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserProfileUrlResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/users")
    public void changePassword(@Auth AuthUser authUser, @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(authUser.getId(), userChangePasswordRequest);
    }

    @PutMapping("/users/profile-image")
    public ResponseEntity<UserProfileUrlResponse> updateProfileImage(
        @Auth AuthUser authUser,
        @RequestParam("profileImage") MultipartFile profileImage
    ) {
        return ResponseEntity.ok(userService.updateProfileImage(authUser.getId(), profileImage));
    }

    @DeleteMapping("/users/profile-image")
    public ResponseEntity<Void> deleteProfileImage(@Auth AuthUser authUser) {
        userService.deleteProfileImage(authUser.getId());
        return ResponseEntity.noContent().build();
    }
}
