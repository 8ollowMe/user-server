package com.followme.userserver.application.dto;

import com.followme.userserver.domain.enums.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserStatusUpdateRequestDto {
    // 변경하고자 하는 타겟 상태값 (APPROVED 또는 REJECTED)
    private UserStatus status;
}