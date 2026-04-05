package com.followme.userserver.infrastructure.keycloak;

import java.util.Collections;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.followme.userserver.application.port.AuthPort;
import com.followme.userserver.exception.KeycloakSyncException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakAdapter implements AuthPort {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;


    @Override
    public void syncKeycloakUserStatus(UUID keycloakUserId, boolean isEnabled) {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(keycloakUserId.toString());
            UserRepresentation kcUser = userResource.toRepresentation();
            
            kcUser.setEnabled(isEnabled);
            userResource.update(kcUser);
            
            log.info("Keycloak user status updated successfully. userId: {}, isEnabled: {}", keycloakUserId, isEnabled);
        } catch (Exception e) {
            log.error("Failed to sync user status with Keycloak. userId: {}", keycloakUserId, e);
            throw new KeycloakSyncException();
        }
    }

    
    @Override
    public void assignRealmRole(UUID keycloakUserId, String roleName) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            RoleRepresentation roleToAssign = realmResource.roles().get(roleName).toRepresentation();
            UserResource userResource = realmResource.users().get(keycloakUserId.toString());
            
            userResource.roles().realmLevel().add(Collections.singletonList(roleToAssign));
            
            log.info("Keycloak role [{}] assigned to user [{}] successfully.", roleName, keycloakUserId);
        } catch (Exception e) {
            log.error("Failed to assign role [{}] to Keycloak user [{}].", roleName, keycloakUserId, e);
            throw new KeycloakSyncException();
        }
    }
}