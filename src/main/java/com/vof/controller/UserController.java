package com.vof.controller;

import com.vof.dto.request.UpdateUserProfileRequest;
import com.vof.dto.response.CommonApiResponse;
import com.vof.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<CommonApiResponse> getMyProfile() {

        return ResponseEntity.ok(
                CommonApiResponse.builder()
                        .success(true)
                        .message("Profile retrieved successfully.")
                        .data(userService.getMyProfile())
                        .build()
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<CommonApiResponse> updateMyProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {

        return ResponseEntity.ok(
                CommonApiResponse.builder()
                        .success(true)
                        .message("Profile updated successfully.")
                        .data(userService.updateMyProfile(request))
                        .build()
        );
    }
}