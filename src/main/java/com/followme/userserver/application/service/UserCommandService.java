package com.followme.userserver.application.service;

import com.followMe.common.event.Events;
import com.followMe.common.exception.BusinessException;
import com.followMe.common.exception.CommonErrorCode;
import com.followme.userserver.application.dto.UserRequest;
import com.followme.userserver.application.dto.UserResponse;
import com.followme.userserver.application.mapper.UserMapper;
import com.followme.userserver.application.port.AuthPort;
import com.followme.userserver.domain.entity.User;
import com.followme.userserver.domain.enums.UserRole;
import com.followme.userserver.domain.enums.UserStatus;
import com.followme.userserver.domain.event.UserCreatedEvent;
import com.followme.userserver.domain.event.UserDeactivatedEvent;
import com.followme.userserver.domain.event.UserStatusUpdatedEvent;
import com.followme.userserver.domain.repository.UserRepository;
import com.followme.userserver.exception.DuplicateUsernameException;
import com.followme.userserver.exception.InvalidDeliveryAssociationException;
import com.followme.userserver.exception.KeycloakSyncException;
import com.followme.userserver.exception.UserNotFoundException;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Keycloak keycloak;
    private final AuthPort authPort;

    @Value("${keycloak.realm}")
    private String realm;
    
    @Transactional
    public void registerUser(UserRequest.Register request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException();
        }

        // 배송 기사(DELIVERY)는 VendorId를 가질 수 없음을 검증
        if (request.getRole() == UserRole.DELIVERY) {
            if (request.getVendorId() != null) {
                throw new InvalidDeliveryAssociationException();
            }
        }

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(request.getUsername());
        kcUser.setEnabled(false);
        kcUser.setLastName(request.getName());
        kcUser.setFirstName(".");
        kcUser.setEmail(request.getUsername() + "@test.com");

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);
        
        kcUser.setCredentials(List.of(credential));

        Response response = keycloak.realm(realm).users().create(kcUser);

        if (response.getStatus() != 201) {
            throw new KeycloakSyncException();
        }
        
        String path = response.getLocation().getPath();
        String keycloakUserId = path.substring(path.lastIndexOf('/') + 1);

        User newUser = userMapper.toEntity(request);
        newUser.setId(UUID.fromString(keycloakUserId));
        
        userRepository.save(newUser);

        Events.trigger(
            new UserCreatedEvent(newUser.getId(), newUser.getUsername(), newUser.getRole().name())
        );
    }

    @Transactional
    public UserResponse.Info updateUserProfile(UUID userId, UserRequest.UpdateProfile request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        user.updateProfile(request);
        return userMapper.toResponseDto(user);
    }

    @Transactional
    public void deactivateMyAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String currentTokenUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        user.deactivateAccount(UUID.fromString(currentTokenUserId));
        
        try {
            UserRepresentation kcUser = keycloak.realm(realm).users().get(userId.toString()).toRepresentation();
            kcUser.setEnabled(false);
            keycloak.realm(realm).users().get(userId.toString()).update(kcUser);
        } catch (Exception e) {
            throw new KeycloakSyncException(); 
        }

        Events.trigger(new UserDeactivatedEvent(user.getId()));
    }

    @Transactional
    public void deactivateAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String currentTokenUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        user.deactivateAccount(UUID.fromString(currentTokenUserId));
        
        try {
            UserRepresentation kcUser = keycloak.realm(realm).users().get(userId.toString()).toRepresentation();
            kcUser.setEnabled(false);
            keycloak.realm(realm).users().get(userId.toString()).update(kcUser);
        } catch (Exception e) {
            throw new KeycloakSyncException(); 
        }

        Events.trigger(new UserDeactivatedEvent(user.getId()));
    }

    @Transactional
    public UserResponse.Info updateUserStatus(UUID userId, UserRequest.UpdateStatus request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (request.getStatus() == UserStatus.APPROVED) {
            user.approve();
            authPort.syncKeycloakUserStatus(userId, true);
            authPort.assignRealmRole(userId, user.getRole().name());


            if (user.getRole() == UserRole.DELIVERY) {
                Long currentMaxSequence = 0L;
                
                if (user.getHubId() != null) {

                    currentMaxSequence = userRepository.findMaxSequenceByHubIdAndRole(user.getHubId(), UserRole.DELIVERY);
                } else {

                    currentMaxSequence = userRepository.findMaxSequenceGlobalByRole(UserRole.DELIVERY);
                }
                
                user.updateSequence(currentMaxSequence + 1);
            }

        } else if (request.getStatus() == UserStatus.REJECTED) {
            user.reject();
            authPort.syncKeycloakUserStatus(userId, false);
            
            user.updateSequence(null);

        } else {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }

        Events.trigger(
            new UserStatusUpdatedEvent(user.getId(), request.getStatus().name())
        );

        return userMapper.toResponseDto(user);
    }
}