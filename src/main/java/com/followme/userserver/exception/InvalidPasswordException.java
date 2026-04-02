package com.followme.userserver.exception;

import com.followMe.common.exception.BusinessException;

public class InvalidPasswordException extends BusinessException {
    public InvalidPasswordException() {
        super(UserErrorCode.INVALID_PASSWORD);
    }
}