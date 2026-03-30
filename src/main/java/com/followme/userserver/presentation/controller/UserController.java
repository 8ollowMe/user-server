package com.followme.userserver.presentation.controller;

import com.followMe.common.response.ApiResponse;
import com.followme.userserver.application.annotation.CurrentUser;
import com.followme.userserver.application.dto.UserRegisterRequestDto;
import com.followme.userserver.application.dto.UserResponseDto;
import com.followme.userserver.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody UserRegisterRequestDto request) {
        
        userService.registerUser(request);

        return ApiResponse.created();
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyProfile(@CurrentUser String username) {
        
        UserResponseDto responseDto = userService.getUserProfile(username);
        
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}