package com.followme.userserver.exception;

import com.followMe.common.exception.BusinessException;

public class DuplicateUsernameException extends BusinessException {
    public DuplicateUsernameException() {
        super(UserErrorCode.DUPLICATE_USERNAME);
    }
}