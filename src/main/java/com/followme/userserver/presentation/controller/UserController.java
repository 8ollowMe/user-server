package com.followme.userserver.presentation.controller;

import com.followMe.common.response.ApiResponse;
import com.followme.userserver.application.annotation.CurrentUser;
import com.followme.userserver.application.dto.UserRegisterRequestDto;
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

    @GetMapping("/me-test")
    public ResponseEntity<ApiResponse> testCurrentUser(@CurrentUser String username) {
        // 토큰에서 뽑아온 username을 그대로 응답 데이터(data)로 내려보내 봅니다.
        return ResponseEntity.ok(ApiResponse.success(username));
    }
}