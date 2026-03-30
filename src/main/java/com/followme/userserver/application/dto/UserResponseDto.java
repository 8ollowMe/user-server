package com.followme.userserver.application.dto;

import com.followme.userserver.domain.enums.UserRole;
import com.followme.userserver.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private UUID userId;
    private String username;
    private String name;
    private String address;
    private String phone;
    private String slackId;
    private UserRole role;
    private UserStatus status;
    private UUID hubId;
    private UUID vendorId;
}