package com.followme.userserver.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.followMe.common.response.ApiResponse;
import com.followme.userserver.application.dto.UserResponse;
import com.followme.userserver.application.service.InternalUserQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/users")
public class InternalUserController {

    private final InternalUserQueryService internalUserQueryService;


    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserInternal(@PathVariable UUID userId) {
        UserResponse.Internal response = internalUserQueryService.getUserInternal(userId);
        return ApiResponse.ok(response);
    }


    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getUsersInternal(@RequestBody List<UUID> userIds) {
        List<UserResponse.Internal> responseList = internalUserQueryService.getUsersInternal(userIds);
        return ApiResponse.ok(responseList);
    }
    
    @GetMapping("/{userId}/slack-id")
    public ResponseEntity<ApiResponse> getUserSlackId(@PathVariable UUID userId) {
        String slackId = internalUserQueryService.getUserSlackId(userId);
        return ApiResponse.ok(slackId);
    }

    @GetMapping("/hub/{hubId}/managers")
    public ResponseEntity<ApiResponse> getManagersByHub(@PathVariable UUID hubId) {
        List<UserResponse.Internal> responseList = internalUserQueryService.getManagersByHub(hubId);
        return ApiResponse.ok(responseList);
    }

    @GetMapping("/deliveries")
    public ResponseEntity<ApiResponse> getDeliveryManagers(
            @RequestParam("hubId") UUID hubId, // 명세서 스펙 상 이름은 hubId지만 vendorId 역할도 겸함
            @RequestParam("type") String type) {
        
        List<UserResponse.Internal> responseList = internalUserQueryService.getDeliveryManagers(hubId, type);
        
        return ApiResponse.ok(responseList);
    }
}