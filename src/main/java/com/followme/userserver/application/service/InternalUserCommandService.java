package com.followme.userserver.application.service;

import com.followme.userserver.domain.entity.User;
import com.followme.userserver.domain.enums.UserRole;
import com.followme.userserver.domain.repository.UserRepository;
import com.followme.userserver.exception.InvalidDeliveryAssociationException;
import com.followme.userserver.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalUserCommandService {

    private final UserRepository userRepository;

    @Transactional
    public void updateDeliverySequenceToLast(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getRole() != UserRole.DELIVERY) {
            throw new IllegalArgumentException("User is not a delivery personnel.");
        }

        Long currentMaxSequence = 0L;

        if (user.getHubId() != null) {
            currentMaxSequence = userRepository.findMaxSequenceByHubIdAndRole(user.getHubId(), UserRole.DELIVERY);
        } else {
            currentMaxSequence = userRepository.findMaxSequenceGlobalByRole(UserRole.DELIVERY);
        }

        user.updateSequence(currentMaxSequence + 1);
    }
}