package com.followme.userserver.application.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.followme.userserver.domain.enums.UserRole;
import com.followme.userserver.domain.enums.UserStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponse {

    @Getter
    @Builder
    public static class Info {
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

    @Getter
    @Builder
    public static class Internal {
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
}