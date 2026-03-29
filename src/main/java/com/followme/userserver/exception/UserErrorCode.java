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
    INVALID_PASSWORD("U003", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED); // 401 Unauthorized

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}