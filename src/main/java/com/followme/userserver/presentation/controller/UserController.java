package com.followme.userserver.presentation.controller;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.common.response.ApiResponse;
import com.followme.userserver.application.annotation.CurrentUser;
import com.followme.userserver.application.dto.UserRequest;
import com.followme.userserver.application.dto.UserResponse;
import com.followme.userserver.application.service.UserCommandService;
import com.followme.userserver.application.service.UserQueryService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @PostMapping("/register")
    @SecurityRequirements()
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody UserRequest.Register request) {
        userCommandService.registerUser(request);
        return ApiResponse.created();
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyProfile(
            @Parameter(hidden = true) @CurrentUser UUID userId) {
        UserResponse.Info responseDto = userQueryService.getUserProfile(userId);
        return ApiResponse.ok(responseDto);
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse> updateMyProfile(
            @Parameter(hidden = true) @CurrentUser UUID userId,
            @RequestBody UserRequest.UpdateProfile request) {
        UserResponse.Info responseDto = userCommandService.updateUserProfile(userId, request);
        return ApiResponse.ok(responseDto);
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse> deactivateMyAccount(
            @Parameter(hidden = true) @CurrentUser UUID userId) {
        userCommandService.deactivateMyAccount(userId);
        return ApiResponse.ok();
    }
    
    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse> updateUserStatus(
            @PathVariable UUID userId,
            @RequestBody UserRequest.UpdateStatus request) {
        UserResponse.Info responseDto = userCommandService.updateUserStatus(userId, request);
        return ApiResponse.ok(responseDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable UUID userId) {
        UserResponse.Info responseDto = userQueryService.getUserById(userId);
        return ApiResponse.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = com.followMe.common.pagination.PageRequest.of(page, size).toPageable();
        PageResponse<UserResponse.Info> responseDto = userQueryService.getAllUsers(pageable);
        return ApiResponse.ok(responseDto);
    }
}