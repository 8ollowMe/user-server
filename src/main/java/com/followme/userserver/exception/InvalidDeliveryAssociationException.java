package com.followme.userserver.exception;

import com.followMe.common.exception.BusinessException;

public class InvalidDeliveryAssociationException extends BusinessException {
    
    public InvalidDeliveryAssociationException() {
        super(UserErrorCode.INVALID_DELIVERY_ASSOCIATION);
    }
}