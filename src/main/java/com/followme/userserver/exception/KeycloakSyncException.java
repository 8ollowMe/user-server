package com.followme.userserver.exception;

import com.followMe.common.exception.BusinessException;

public class KeycloakSyncException extends BusinessException {
    public KeycloakSyncException() {
        super(UserErrorCode.KEYCLOAK_SYNC_FAILED);
    }
}