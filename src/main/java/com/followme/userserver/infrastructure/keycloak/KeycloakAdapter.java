package com.followme.userserver.infrastructure.keycloak;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.followme.userserver.exception.KeycloakSyncException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakAdapter {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public void syncKeycloakUserStatus(UUID keycloakUserId, boolean isEnabled) {
        try {
            // 1. ID로 Keycloak 유저 리소스에 직접 접근
            UserResource userResource = keycloak.realm(realm).users().get(keycloakUserId.toString());
            
            // 2. 현재 유저의 표현(Representation) 객체를 가져옴
            UserRepresentation kcUser = userResource.toRepresentation();
            
            // 3. Enabled 상태 변경
            kcUser.setEnabled(isEnabled);
            
            // 4. Keycloak 서버에 업데이트 요청
            userResource.update(kcUser);
            
            log.info("Keycloak user status updated successfully. userId: {}, isEnabled: {}", keycloakUserId, isEnabled);
            
        } catch (Exception e) {
            log.error("Failed to sync user status with Keycloak. userId: {}", keycloakUserId, e);
            throw new KeycloakSyncException();
        }
    }
}