package com.followme.userserver.application.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInternalResponseDto {
    private UUID userId;
    private String username;
    private String name;
    private String role;
    private String slackId;
    private UUID hubId;
}