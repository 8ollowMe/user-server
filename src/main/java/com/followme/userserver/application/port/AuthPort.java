package com.followme.userserver.application.port;

import java.util.UUID;

public interface AuthPort {

    void syncKeycloakUserStatus(UUID userId, boolean isEnabled);
    
    void assignRealmRole(UUID userId, String roleName);
    
}