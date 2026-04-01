package com.followme.userserver.domain.enums;

public enum UserStatus {
    PENDING,    // 승인 대기 (가입 시 기본값)
    APPROVED,   // 승인 완료
    REJECTED,   // 승인 거절
    DELETED     // 탈퇴/삭제
}