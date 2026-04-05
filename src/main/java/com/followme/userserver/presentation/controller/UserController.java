package com.followme.userserver.presentation.controller;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.common.response.ApiResponse;
import com.followme.userserver.application.annotation.CurrentUser;
import com.followme.userserver.application.dto.UserRegisterRequestDto;
import com.followme.userserver.application.dto.UserResponseDto;
import com.followme.userserver.application.dto.UserStatusUpdateRequestDto;
import com.followme.userserver.application.dto.UserUpdateRequestDto;
import com.followme.userserver.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse> updateMyProfile(
            @CurrentUser String username,
            @RequestBody UserUpdateRequestDto request) {

        UserResponseDto responseDto = userService.updateUserProfile(username, request);
        
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse> deactivateMyAccount(@CurrentUser String username) {
        
        userService.deactivateMyAccount(username);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse> updateUserStatus(
            @PathVariable UUID userId,
            @RequestBody UserStatusUpdateRequestDto request) {
        
        UserResponseDto responseDto = userService.updateUserStatus(userId, request);
        
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable UUID userId) {
        
        UserResponseDto responseDto = userService.getUserById(userId);
        
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 1. common-lib의 PageRequest를 통해 검증된 Pageable 객체 생성 (10, 30, 50 사이즈 강제)
        Pageable pageable = PageRequest.of(page, size).toPageable();

        PageResponse<UserResponseDto> responseDto = userService.getAllUsers(pageable);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}