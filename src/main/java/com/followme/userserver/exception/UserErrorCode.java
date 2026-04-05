package com.followme.userserver.exception;

import com.followMe.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    
    // User 서비스만의 고유 에러 코드
    DUPLICATE_USERNAME("U001", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT), // 409 Conflict
    USER_NOT_FOUND("U002", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),     // 404 Not Found
    INVALID_PASSWORD("U003", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED), // 401 Unauthorized
    KEYCLOAK_SYNC_FAILED("U004", "인증 서버에 유저를 생성하지 못했습니다.", HttpStatus.INTERNAL_SERVER_ERROR), // 500 Internal Server Error

    INVALID_DELIVERY_ASSOCIATION("U005", "배송 기사는 허브 또는 업체 중 단 하나의 소속만 지정되어야 합니다.", HttpStatus.BAD_REQUEST); // 400 Bad Request

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}